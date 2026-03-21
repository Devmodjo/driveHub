package cm.mvtech.drivehub.platform.admin.services.serviceImpl;

import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminAuthResponse;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminCreateRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminLoginRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminResponse;
import cm.mvtech.drivehub.platform.admin.models.mappers.PlatformAdminMapper;
import cm.mvtech.drivehub.platform.admin.repositories.PlatformAdminRepository;
import cm.mvtech.drivehub.platform.admin.services.AdminerService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PlatformAdminMapper adminMapper;

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

    /**
     * Activation d'un compte admin
     * Accessible uniquement par ROOT
     */
    @Override
    public void activateAdmin(UUID monitorId) {

        log.info("Tentative d'activation de l'admin {}", monitorId);

        PlatformAdmin admin = adminRepository.findById(monitorId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Cet administrateur n'existe pas !"
                ));

        // Activer le compte
        admin.setAdminStatus(AdminStatus.ACTIVE);
        adminRepository.save(admin);

        log.info("Admin {} activé avec succès", admin.getEmail());
    }
}