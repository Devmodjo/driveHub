package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.modules.auth.application.dto.ForgotPasswordRequest;
import cm.mvtech.drivehub.modules.auth.application.dto.ResetPasswordRequest;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.platform.admin.models.AdminEmailVerificationToken;
import cm.mvtech.drivehub.platform.admin.models.AdminPasswordResetToken;
import cm.mvtech.drivehub.platform.admin.models.AdminPrincipal;
import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.dto.*;
import cm.mvtech.drivehub.platform.admin.models.mappers.PlatformAdminMapper;
import cm.mvtech.drivehub.platform.admin.repositories.AdminEmailVerificationTokenRepository;
import cm.mvtech.drivehub.platform.admin.repositories.AdminPasswordResetTokenRepository;
import cm.mvtech.drivehub.platform.admin.repositories.PlatformAdminRepository;
import cm.mvtech.drivehub.platform.admin.services.AdminerService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PlatformAdminService implements AdminerService {

    private final PlatformAdminRepository adminRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PlatformAdminMapper adminMapper;
    private final AdminEmailVerificationTokenRepository adminEmailTokenRepository;
    private final AdminPasswordResetTokenRepository adminPasswordResetRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.root-admin.email}")
    private String rootEmail;

    @Override
    public PlatformAdminAuthResponse adminerLogin(PlatformAdminLoginRequest request) {
        PlatformAdmin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new AccessDeniedException("Email ou mot de passe incorrect");
        }

        if (admin.getAdminStatus() == AdminStatus.EMAIL_PENDING) {
            throw new AccessDeniedException(
                    "Veuillez vérifier votre adresse email avant de vous connecter");
        }

        if (admin.getAdminStatus() == AdminStatus.PENDING) {
            throw new AccessDeniedException(
                    "Votre compte est en attente de validation par l'administrateur ROOT");
        }

        if (admin.getAdminStatus() != AdminStatus.ACTIVE) {
            throw new AccessDeniedException("Compte suspendu ou désactivé");
        }

        return new PlatformAdminAuthResponse(
                jwtService.generatePlatformAdminToken(admin),
                admin.getRole(),
                admin.getAdminStatus()
        );
    }

    @Override
    public void adminerRegistry(PlatformAdminCreateRequest request) {
        if (request.role() == AdminRole.ROOT) {
            throw new IllegalArgumentException(
                    "Vous ne pouvez pas vous inscrire en tant que ROOT.");
        }

        if (adminRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé !");
        }

        PlatformAdmin admin = new PlatformAdmin();
        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setRole(request.role());
        admin.setPassword(passwordEncoder.encode(request.password()));
        admin.setAdminStatus(AdminStatus.EMAIL_PENDING);
        admin.setPhoneNumber(request.phoneNumber());
        admin.setResidence(request.residence());
        admin.setReason(request.reason());
        adminRepository.save(admin);

        sendAdminVerificationEmail(admin.getEmail());
//        emailService.sendNewAdminRegistrationNotification(
//                rootEmail, admin.getName(),
//                admin.getEmail(), admin.getRole().toString(), admin.getResidence(),
//                admin.getPhoneNumber(), admin.getReason(), admin.getCreatedAt().toString(),
//                admin.getId()
//        );
        log.info("Admin {} créé — email de vérification envoyé", request.email());
    }

    @Override
    public List<PlatformAdminResponse> pendingAdminerRequest() {
        return adminRepository.findByAdminStatus(AdminStatus.PENDING)
                .stream()
                .map(adminMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformAdminResponse getCurrentAdmin(Authentication authentication) {
        AdminPrincipal principal = extractAdminPrincipal(authentication);
        return new PlatformAdminResponse(
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getRole(),
                principal.getStatus(),
                principal.getResidence(),
                principal.getPhoneNumber(),
                principal.getCreatedAt()
        );
    }

    @Override
    public void activateAdmin(UUID adminId) {
        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Administrateur introuvable"));

        admin.setAdminStatus(AdminStatus.ACTIVE);
        adminRepository.save(admin);
        log.info("Admin {} activé", admin.getEmail());

        emailService.sendAdminWelcomeMail(
                admin.getEmail(),
                admin.getName(),
                "ACTIVE",
                admin.getRole().name(),
                null
        );
    }

    @Override
    public Page<PlatformAdminResponse> getAllAdmins(Pageable pageable,
                                                    AdminStatus status,
                                                    AdminRole role) {
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
        return adminMapper.toResponse(
                adminRepository.findById(adminId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Administrateur introuvable : " + adminId))
        );
    }

    @Override
    public PlatformAdminResponse updateAdmin(UUID adminId, UpdateAdminRequest request) {
        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrateur introuvable : " + adminId));

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
                        "Administrateur introuvable : " + adminId));

        if (admin.getRole() == AdminRole.ROOT) {
            throw new AccessDeniedException("Impossible de désactiver un compte ROOT");
        }

        admin.setAdminStatus(AdminStatus.SUSPENDED);
        adminRepository.save(admin);
        log.info("Admin {} suspendu", admin.getEmail());
    }

    @Override
    public void deleteAdmin(UUID adminId) {
        PlatformAdmin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrateur introuvable : " + adminId));

        if (admin.getRole() == AdminRole.ROOT) {
            throw new AccessDeniedException("Impossible de supprimer un compte ROOT");
        }

        adminRepository.delete(admin);
        log.info("Admin {} supprimé", admin.getEmail());
    }

    @Override
    public PlatformAdminResponse updateMyProfile(Authentication authentication,
                                                 UpdateAdminRequest request) {
        AdminPrincipal principal = extractAdminPrincipal(authentication);
        PlatformAdmin admin = adminRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Admin introuvable"));

        if (!admin.getEmail().equals(request.email()) &&
                adminRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        admin.setName(request.name());
        admin.setEmail(request.email());
        admin.setResidence(request.residence());
        admin.setPhoneNumber(request.phoneNumber());

        return adminMapper.toResponse(adminRepository.save(admin));
    }

    @Override
    public void changeMyPassword(Authentication authentication,
                                 ChangePasswordRequest request) {
        AdminPrincipal principal = extractAdminPrincipal(authentication);
        PlatformAdmin admin = adminRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Admin introuvable"));

        if (!passwordEncoder.matches(request.oldPassword(), admin.getPassword())) {
            throw new AccessDeniedException("Ancien mot de passe incorrect");
        }

        admin.setPassword(passwordEncoder.encode(request.newPassword()));
        adminRepository.save(admin);
        log.info("Mot de passe modifié pour l'admin {}", admin.getEmail());
    }

    @Override
    public AdminStatsResponse getAdminStats() {
        long total    = adminRepository.count();
        long pending  = adminRepository.countByAdminStatus(AdminStatus.PENDING);
        long active   = adminRepository.countByAdminStatus(AdminStatus.ACTIVE);
        long inactive = adminRepository.countByAdminStatus(AdminStatus.SUSPENDED)
                + adminRepository.countByAdminStatus(AdminStatus.DISABLED);
        return new AdminStatsResponse(total, pending, active, inactive);
    }

    @Override
    public void sendAdminVerificationEmail(String email) {
        PlatformAdmin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Admin introuvable : " + email));

        adminEmailTokenRepository.deleteAllByAdminId(admin.getId());

        String token = UUID.randomUUID().toString();
        AdminEmailVerificationToken verificationToken = new AdminEmailVerificationToken();
        verificationToken.setAdmin(admin);
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationToken.setUsed(false);
        adminEmailTokenRepository.save(verificationToken);

        emailService.sendAdminEmailVerification(
                admin.getEmail(),
                admin.getName(),
                admin.getEmail(),
                admin.getRole().name(),
                frontendUrl + "/backoffice/verify-email?token=" + token
        );
    }

    @Override
    @Transactional
    public void verifyAdminEmail(String token) {
        AdminEmailVerificationToken verificationToken =
                adminEmailTokenRepository.findByToken(token)
                        .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("Token expiré — demandez un nouveau lien");
        }

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Token déjà utilisé");
        }

        PlatformAdmin admin = verificationToken.getAdmin();

        if (admin.getAdminStatus() == AdminStatus.EMAIL_PENDING) {
            admin.setAdminStatus(AdminStatus.PENDING);
            adminRepository.save(admin);

            emailService.sendAdminWelcomeMail(
                    admin.getEmail(),
                    admin.getName(),
                    "PENDING",
                    admin.getRole().name(),
                    null
            );

            adminRepository.findByRole(AdminRole.ROOT).forEach(root ->
                    emailService.sendNewAdminRegistrationNotification(
                            root.getEmail(),
                            admin.getName(),
                            admin.getEmail(),
                            admin.getRole().name(),
                            admin.getResidence(),
                            admin.getPhoneNumber(),
                            admin.getReason(),
                            LocalDate.now().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            admin.getId()
                    )
            );
        }

        verificationToken.setUsed(true);
        adminEmailTokenRepository.save(verificationToken);
        log.info("Email vérifié pour l'admin {}", admin.getEmail());
    }

    @Override
    public void adminForgotPassword(ForgotPasswordRequest request) {
        adminRepository.findByEmail(request.email()).ifPresent(admin -> {
            adminPasswordResetRepository.deleteAllByAdminId(admin.getId());

            String token = UUID.randomUUID().toString();
            AdminPasswordResetToken resetToken = new AdminPasswordResetToken();
            resetToken.setAdmin(admin);
            resetToken.setToken(token);
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            adminPasswordResetRepository.save(resetToken);

            emailService.sendPasswordResetEmail(
                    admin.getEmail(),
                    admin.getName(),
                    frontendUrl + "/backoffice/reset-password?token=" + token
            );
        });
    }

    @Override
    @Transactional
    public void adminResetPassword(ResetPasswordRequest request) {
        AdminPasswordResetToken resetToken =
                adminPasswordResetRepository.findByToken(request.token())
                        .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token expiré — refaites la demande");
        }

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token déjà utilisé");
        }

        PlatformAdmin admin = resetToken.getAdmin();
        admin.setPassword(passwordEncoder.encode(request.newPassword()));
        adminRepository.save(admin);

        resetToken.setUsed(true);
        adminPasswordResetRepository.save(resetToken);
        log.info("Mot de passe réinitialisé pour l'admin {}", admin.getEmail());
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
}