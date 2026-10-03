package cm.mvtech.drivehub.modules.monitor.domain.services;

import cm.mvtech.drivehub.modules.auth.application.dto.AcceptInvitationRequest;
import cm.mvtech.drivehub.modules.auth.domain.model.PasswordResetToken;
import cm.mvtech.drivehub.modules.auth.domain.model.PrivacyPolicy;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.PasswordResetTokenRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.document.domain.model.DocumentType;
import cm.mvtech.drivehub.modules.document.domain.services.DocumentService;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.SchoolOwnerGuard;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.monitor.application.dto.AddMonitorRequest;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.infrastructure.mapper.MonitorMapper;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Ajout d'un moniteur par le responsable de l'auto-école, puis activation du compte par le moniteur.
 *
 * <p>Parcours :</p>
 * <ol>
 *   <li>le responsable saisit l'identité du moniteur et joint sa CNI et son CAPEC (POST /api/monitors) ;</li>
 *   <li>le compte est créé sans mot de passe utilisable, rattaché à l'auto-école (fiche moniteur dans le
 *       schéma du tenant + demande d'adhésion « acceptée » dans le schéma public, utilisée pour retrouver
 *       l'auto-école à la connexion) ; les pièces sont enregistrées comme vérifiées par le responsable ;</li>
 *   <li>le moniteur reçoit un email d'invitation (lien valable 72 h) ; il choisit son mot de passe et
 *       accepte la politique de confidentialité (POST /api/auth/accept-invitation).</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class MonitorInvitationService {

    /** Durée de validité du lien d'invitation. */
    static final int INVITATION_HOURS = 72;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SchoolOwnerGuard schoolAccess;
    private final DrivingSchoolRegistryRepository registryRepository;
    private final CurrentSchoolProvider currentSchoolProvider;
    private final UserRepository userRepository;
    private final MonitorsRepository monitorsRepository;
    private final SchoolJoinRequestRepository joinRequestRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final DocumentService documentService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final MonitorMapper mapper;

    @Transactional
    public MonitorResponseDto addMonitor(AddMonitorRequest request, MultipartFile cni, String cniNumber,
                                         MultipartFile capec, String capecNumber) {
        User owner = schoolAccess.requireOwnerOfCurrentSchool();
        DrivingSchoolRegistry registry = registryRepository.findByAdmin(owner)
                .orElseThrow(() -> new BadRequestException("Auto-école introuvable"));

        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Un compte DriveHub existe déjà avec cette adresse email. "
                    + "Demandez à cette personne d'envoyer une demande d'adhésion depuis son espace.");
        }

        // 1. Compte (schéma public). Le mot de passe est un secret aléatoire jamais communiqué :
        //    impossible de se connecter avant d'avoir accepté l'invitation.
        User user = new User();
        user.setFirstname(request.firstname().trim());
        user.setLastname(request.lastname() == null ? null : request.lastname().trim());
        user.setEmail(email);
        user.setRoles(Role.MONITOR);
        user.setPassword(passwordEncoder.encode(randomToken()));
        user.setProfileStatus(ProfileStatus.REGISTERED);
        user = userRepository.save(user);

        // 2. Fiche moniteur dans le schéma de l'auto-école (tenant courant)
        Monitor monitor = new Monitor();
        monitor.setUser(user);
        monitor.setPhoneNumber(request.phoneNumber());
        monitor.setGender(request.gender());
        monitor.setNationality(request.nationality());
        monitor.setResidenceCity(request.residenceCity());
        monitor.setDateOfBirth(request.dateOfBirth());
        monitor.setDrivingSchool(currentSchoolProvider.get());
        monitor = monitorsRepository.save(monitor);

        // 3. Rattachement (schéma public) : la connexion retrouve l'auto-école grâce à cette demande acceptée
        SchoolJoinRequest link = new SchoolJoinRequest();
        link.setUser(user);
        link.setDrivingSchoolId(registry.getId());
        link.setRequestedRole(Role.MONITOR);
        link.setJoinStatus(JoinStatus.APPROVED);
        joinRequestRepository.save(link);

        // 4. Justificatifs, vérifiés par le responsable qui les a fournis
        documentService.uploadVerified(user, DocumentType.CNI, cni, cniNumber, owner.getEmail());
        documentService.uploadVerified(user, DocumentType.CAPEC, capec, capecNumber, owner.getEmail());

        // 5. Invitation
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(randomToken());
        token.setExpiresAt(LocalDateTime.now().plusHours(INVITATION_HOURS));
        token.setUsed(false);
        tokenRepository.save(token);
        emailService.sendMonitorInvitationEmail(email, user.getFirstname(), registry.getSchoolName(), token.getToken());

        return mapper.fromEntityToResponse(monitor);
    }

    /** Le moniteur invité choisit son mot de passe : son compte devient actif. */
    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {
        if (!request.acceptPrivacyPolicy()) {
            throw new BadRequestException("Vous devez accepter la politique de confidentialité pour activer votre compte");
        }
        PasswordResetToken token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadRequestException("Lien d'invitation invalide"));
        if (token.isUsed()) {
            throw new BadRequestException("Ce lien d'invitation a déjà été utilisé : connectez-vous avec votre mot de passe");
        }
        if (token.isExpired()) {
            throw new BadRequestException("Ce lien d'invitation a expiré : demandez au responsable de l'auto-école de vous ajouter à nouveau, "
                    + "ou utilisez « Mot de passe oublié »");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPrivacyPolicyAcceptedAt(LocalDateTime.now());
        user.setPrivacyPolicyVersion(PrivacyPolicy.CURRENT_VERSION);
        // L'adresse est prouvée (le lien a été reçu par email) et l'auto-école a déjà accepté le moniteur
        user.setProfileStatus(ProfileStatus.ACTIVE);
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    /** Jeton aléatoire de 256 bits, utilisable dans une URL. */
    private static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
