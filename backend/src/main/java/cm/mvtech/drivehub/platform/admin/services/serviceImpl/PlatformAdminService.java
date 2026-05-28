package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.platform.admin.models.AdminPrincipal;
import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.dto.*;
import cm.mvtech.drivehub.platform.admin.models.mappers.PlatformAdminMapper;
import cm.mvtech.drivehub.platform.admin.repositories.PlatformAdminRepository;
import cm.mvtech.drivehub.platform.admin.services.AdminerService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlatformAdminService implements AdminerService {

    private final PlatformAdminRepository adminRepository;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender javaMailSender;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PlatformAdminMapper adminMapper;
    private final EmailService emailService;

    /**
     * Login pour les admins de la plateforme (ROOT, SUPER_ADMIN, ADMIN)
     * PAS BESOIN de tenant - ils gèrent toute la plateforme
     */
    @Override
    public PlatformAdminAuthResponse adminerLogin(PlatformAdminLoginRequest loginRequest) {

        log.info("Tentative de login admin plateforme pour {}", loginRequest.email());

        // Charger l'admin
        PlatformAdmin admin = adminRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("utilisateur introuvable"));

        // Vérifier le mot de passe manuellement
        if (!passwordEncoder.matches(loginRequest.password(), admin.getPassword())) {
            throw new AccessDeniedException("Email ou mot de passe incorrect");
        }

        // Vérifier que le compte est actif
        if (admin.getAdminStatus() != AdminStatus.ACTIVE) {
            throw new AccessDeniedException("Compte non activé");
        }
        String adminToken = jwtService.generatePlatformAdminToken(admin);
        return new PlatformAdminAuthResponse(
                adminToken,
                admin.getRole(),
                admin.getAdminStatus()
        );
    }

    /**
     * Inscription d'un nouvel admin plateforme
     * Le compte sera PENDING jusqu'à validation par un ROOT
     */
    @Override
    public void adminerRegistry(PlatformAdminCreateRequest adminCreateRequest) {

        log.info("Inscription admin plateforme pour {}", adminCreateRequest.email());

        // 1. Vérifier qu'on ne peut pas s'inscrire en tant que ROOT
        if (adminCreateRequest.role() == AdminRole.ROOT) {
            throw new IllegalArgumentException(
                    "Vous ne pouvez pas vous inscrire en tant que ROOT. " +
                            "Seul un ROOT existant peut créer un autre ROOT."
            );
        }

        // 2. Vérifier email unique
        if (adminRepository.findByEmail(adminCreateRequest.email()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé !");
        }

        // 3. Créer l'admin (PENDING par défaut)
        PlatformAdmin admin = new PlatformAdmin();
        admin.setName(adminCreateRequest.name());
        admin.setEmail(adminCreateRequest.email());
        admin.setRole(adminCreateRequest.role());
        admin.setPassword(passwordEncoder.encode(adminCreateRequest.password()));
        admin.setAdminStatus(AdminStatus.PENDING); // En attente de validation
        admin.setPhoneNumber(adminCreateRequest.phoneNumber());
        admin.setResidence(adminCreateRequest.residence());

        adminRepository.save(admin);
        emailService.sendAdminWelcomeMail(admin.getEmail(), admin.getName());

        log.info("Admin plateforme {} créé avec succès (PENDING)", adminCreateRequest.email());
    }

    /**
     * Liste des admins en attente de validation
     * Accessible uniquement par ROOT
     */
    @Override
    public List<PlatformAdminResponse> pendingAdminerRequest() {

        log.info("Récupération des admins en attente");

        return adminRepository.findByAdminStatus(AdminStatus.PENDING)
                .stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformAdminResponse getCurrentAdmin(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated())
            throw new AccessDeniedException("Ce endpoint nécessite une authentification");

        Object principal = authentication.getPrincipal();

        if (principal instanceof AdminPrincipal admin) {

            UUID id = admin.getId();
            String name = admin.getUsername();
            String email = admin.getEmail();
            AdminRole adminRole = admin.getRole();
            AdminStatus adminStatus = admin.getStatus();
            LocalDateTime createdAt = admin.getCreatedAt();

            return new PlatformAdminResponse(id, name, email, adminRole, adminStatus, createdAt);
        }

        throw new IllegalArgumentException("Type de principal non supporté : " +
                principal.getClass().getName());
    }

    /**
     * Activation d'un compte admin
     * Accessible uniquement par ROOT
     */
    @Override
    public void activateAdmin(UUID adminId) {

        log.info("Tentative d'activation de l'admin {}", adminId);

        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Cet administrateur n'existe pas !"
                ));

        // Activer le compte
        admin.setAdminStatus(AdminStatus.ACTIVE);
        adminRepository.save(admin);

        log.info("Admin {} activé avec succès", admin.getEmail());
    }
// ─── SECTION 1 — GESTION DES ADMINS ──────────────────────────────────────────

    @Override
    public Page<PlatformAdminResponse> getAllAdmins(Pageable pageable,
                                                    AdminStatus status,
                                                    AdminRole role) {
        // Filtre dynamique selon les paramètres fournis
        if (status != null && role != null) {
            return adminRepository
                    .findByAdminStatusAndRole(status, role, pageable)
                    .map(adminMapper::toResponse);
        }
        if (status != null) {
            return adminRepository
                    .findByAdminStatus(status, pageable)
                    .map(adminMapper::toResponse);
        }
        if (role != null) {
            return adminRepository
                    .findByRole(role, pageable)
                    .map(adminMapper::toResponse);
        }
        return adminRepository.findAll(pageable).map(adminMapper::toResponse);
    }

    @Override
    public PlatformAdminResponse getAdminById(UUID adminId) {
        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrateur introuvable avec l'id : " + adminId));
        return adminMapper.toResponse(admin);
    }

    @Override
    public PlatformAdminResponse updateAdmin(UUID adminId, UpdateAdminRequest request) {
        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrateur introuvable avec l'id : " + adminId));

        // Vérifier unicité email si changé
        if (!admin.getEmail().equals(request.email()) &&
                adminRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setRole(request.role());
        admin.setResidence(request.residence());
        admin.setPhoneNumber(request.phoneNumber());

        return adminMapper.toResponse(adminRepository.save(admin));
    }

    @Override
    public void deactivateAdmin(UUID adminId) {
        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrateur introuvable avec l'id : " + adminId));

        if (admin.getRole() == AdminRole.ROOT) {
            throw new AccessDeniedException("Impossible de désactiver un compte ROOT");
        }

        admin.setAdminStatus(AdminStatus.SUSPENDED);
        adminRepository.save(admin);
        log.info("Admin {} suspendu avec succès", admin.getEmail());
    }

    @Override
    public void deleteAdmin(UUID adminId) {
        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrateur introuvable avec l'id : " + adminId));

        if (admin.getRole() == AdminRole.ROOT) {
            throw new AccessDeniedException("Impossible de supprimer un compte ROOT");
        }

        adminRepository.delete(admin);
        log.info("Admin {} supprimé définitivement", admin.getEmail());
    }

    @Override
    public PlatformAdminResponse updateMyProfile(Authentication authentication,
                                                 UpdateAdminRequest request) {
        AdminPrincipal principal = extractAdminPrincipal(authentication);

        PlatformAdmin admin = adminRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Admin introuvable"));

        // Vérifier unicité email si changé
        if (!admin.getEmail().equals(request.email()) &&
                adminRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setResidence(request.residence());
        admin.setPhoneNumber(request.phoneNumber());
        // Le rôle ne peut pas être changé via ce endpoint

        return adminMapper.toResponse(adminRepository.save(admin));
    }

    @Override
    public void changeMyPassword(Authentication authentication,
                                 ChangePasswordRequest request) {
        AdminPrincipal principal = extractAdminPrincipal(authentication);

        PlatformAdmin admin = adminRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Admin introuvable"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.oldPassword(), admin.getPassword())) {
            throw new AccessDeniedException("Ancien mot de passe incorrect");
        }

        admin.setPassword(passwordEncoder.encode(request.newPassword()));
        adminRepository.save(admin);
        log.info("Mot de passe changé pour l'admin {}", admin.getEmail());
    }

    @Override
    public AdminStatsResponse getAdminStats() {
        long total = adminRepository.count();
        long pending = adminRepository.countByAdminStatus(AdminStatus.PENDING);
        long active = adminRepository.countByAdminStatus(AdminStatus.ACTIVE);
        long inactive = adminRepository.countByAdminStatus(AdminStatus.SUSPENDED) +
                adminRepository.countByAdminStatus(AdminStatus.DISABLED);

        return new AdminStatsResponse(total, pending, active, inactive);
    }



    private AdminPrincipal extractAdminPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentification requise");
        }
        if (!(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            throw new IllegalArgumentException("Type de principal non supporté");
        }
        return principal;
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
    }

}