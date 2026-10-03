package cm.mvtech.drivehub.platform.mailing.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.MailRecipient;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.platform.mailing.application.dto.EmailCampaignResponse;
import cm.mvtech.drivehub.platform.mailing.application.dto.RecipientSuggestion;
import cm.mvtech.drivehub.platform.mailing.application.dto.SendPlatformEmailRequest;
import cm.mvtech.drivehub.platform.mailing.application.dto.SendPlatformEmailResponse;
import cm.mvtech.drivehub.platform.mailing.domain.model.EmailAudience;
import cm.mvtech.drivehub.platform.mailing.domain.model.PlatformEmailCampaign;
import cm.mvtech.drivehub.platform.mailing.infrastructure.repository.PlatformEmailCampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Envoi d'emails depuis le back-office par le propriétaire de la plateforme.
 *
 * <p>Déroulé d'un envoi :</p>
 * <ol>
 *   <li>{@link #resolveRecipients} calcule la liste des destinataires selon l'audience choisie
 *       (doublons supprimés : une même adresse ne reçoit jamais deux fois le message) ;</li>
 *   <li>l'envoi est enregistré dans l'historique (table platform_email_campaigns) ;</li>
 *   <li>{@link EmailService#sendPlatformAnnouncement} envoie les emails en arrière-plan.</li>
 * </ol>
 *
 * <p>Toutes les données utilisées sont dans le schéma public : comptes, registre des auto-écoles et
 * demandes d'adhésion acceptées. Aucun accès aux schémas des auto-écoles n'est nécessaire.</p>
 */
@Service
@RequiredArgsConstructor
public class PlatformEmailService {

    /** Auto-écoles considérées comme ouvertes sur la plateforme. */
    private static final List<DrivingSchoolStatus> OPEN_SCHOOLS =
            List.of(DrivingSchoolStatus.APPROVED, DrivingSchoolStatus.ACTIVE);

    private final UserRepository userRepository;
    private final DrivingSchoolRegistryRepository registryRepository;
    private final SchoolJoinRequestRepository joinRequestRepository;
    private final PlatformEmailCampaignRepository campaignRepository;
    private final EmailService emailService;

    /** Enregistre l'envoi dans l'historique et lance l'envoi en arrière-plan. */
    @Transactional
    public SendPlatformEmailResponse send(SendPlatformEmailRequest request, String adminEmail) {
        List<MailRecipient> recipients = resolveRecipients(request.audience(), request.schoolId(), request.recipients());
        if (recipients.isEmpty()) {
            throw new BadRequestException("Aucun destinataire ne correspond à ce choix : l'email n'a pas été envoyé");
        }

        PlatformEmailCampaign campaign = new PlatformEmailCampaign();
        campaign.setSubject(request.subject().trim());
        campaign.setMessage(request.message().trim());
        campaign.setAudience(request.audience());
        campaign.setRecipientCount(recipients.size());
        campaign.setSentByEmail(adminEmail);
        campaign.setSentAt(LocalDateTime.now());
        if (request.audience() == EmailAudience.SCHOOL_MEMBERS) {
            campaign.setSchoolId(request.schoolId());
            campaign.setSchoolName(findOpenSchool(request.schoolId()).getSchoolName());
        }
        campaignRepository.save(campaign);

        emailService.sendPlatformAnnouncement(recipients, campaign.getSubject(), campaign.getMessage());

        String plural = recipients.size() > 1 ? "s" : "";
        return new SendPlatformEmailResponse(campaign.getId(), recipients.size(),
                "Envoi en cours à " + recipients.size() + " destinataire" + plural);
    }

    /** Nombre de destinataires d'une audience (aperçu avant l'envoi). */
    @Transactional(readOnly = true)
    public int countRecipients(EmailAudience audience, UUID schoolId) {
        if (audience == EmailAudience.INDIVIDUAL) {
            return 0;   // dépend de la liste saisie : calculé par le frontend
        }
        return resolveRecipients(audience, schoolId, null).size();
    }

    /**
     * Destinataires d'une audience, sans doublon (la clé est l'adresse email en minuscules).
     *
     * @throws BadRequestException si l'auto-école ou la liste d'adresses est manquante
     */
    @Transactional(readOnly = true)
    public List<MailRecipient> resolveRecipients(EmailAudience audience, UUID schoolId, List<String> emails) {
        Map<String, MailRecipient> byEmail = new LinkedHashMap<>();
        switch (audience) {
            case ALL_SCHOOLS -> registryRepository.findAllByDrivingSchoolStatusIn(OPEN_SCHOOLS)
                    .forEach(school -> add(byEmail, schoolContactEmail(school), school.getSchoolName()));
            case ALL_MONITORS -> activeUsers(Role.MONITOR).forEach(user -> add(byEmail, user));
            case ALL_STUDENTS -> activeUsers(Role.STUDENT).forEach(user -> add(byEmail, user));
            case SCHOOL_MEMBERS -> {
                if (schoolId == null) {
                    throw new BadRequestException("Choisissez l'auto-école dont les membres recevront l'email");
                }
                DrivingSchoolRegistry school = findOpenSchool(schoolId);
                add(byEmail, school.getAdmin());   // le responsable de l'auto-école
                joinRequestRepository.findAllByDrivingSchoolIdAndJoinStatus(schoolId, JoinStatus.APPROVED)
                        .forEach(member -> add(byEmail, member.getUser()));
            }
            case INDIVIDUAL -> {
                if (emails == null || emails.isEmpty()) {
                    throw new BadRequestException("Ajoutez au moins un destinataire");
                }
                for (String email : emails) {
                    String normalized = email.trim().toLowerCase();
                    String name = userRepository.findByEmail(normalized).map(User::getFirstname).orElse("");
                    add(byEmail, normalized, name);
                }
            }
        }
        return new ArrayList<>(byEmail.values());
    }

    /** Recherche de destinataires (2 caractères minimum) : comptes et auto-écoles validées. */
    @Transactional(readOnly = true)
    public List<RecipientSuggestion> searchRecipients(String query) {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            return List.of();
        }
        List<RecipientSuggestion> results = new ArrayList<>();
        registryRepository.searchByStatus(q, OPEN_SCHOOLS, PageRequest.of(0, 5)).forEach(school ->
                results.add(new RecipientSuggestion(schoolContactEmail(school), school.getSchoolName(), "SCHOOL",
                        school.getSchoolName())));
        userRepository.search(q, PageRequest.of(0, 15)).forEach(user ->
                results.add(new RecipientSuggestion(user.getEmail(), fullName(user), user.getRoles().name(),
                        schoolNameOf(user))));
        return results.stream().limit(20).toList();
    }

    /** Historique des envois, du plus récent au plus ancien. */
    @Transactional(readOnly = true)
    public ApiPageResponse<EmailCampaignResponse> history(int page, int size) {
        return ApiPageResponse.from(campaignRepository
                .findAllByOrderBySentAtDesc(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50)))
                .map(c -> new EmailCampaignResponse(c.getId(), c.getSubject(), c.getMessage(), c.getAudience(),
                        c.getSchoolName(), c.getRecipientCount(), c.getSentByEmail(), c.getSentAt())));
    }

    // ------------------------------------------------------------------ outils

    private List<User> activeUsers(Role role) {
        return userRepository.findAllByRolesAndProfileStatusNot(role, ProfileStatus.SUSPENDED);
    }

    private DrivingSchoolRegistry findOpenSchool(UUID schoolId) {
        return registryRepository.findById(schoolId)
                .filter(school -> OPEN_SCHOOLS.contains(school.getDrivingSchoolStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Auto-école introuvable ou non validée"));
    }

    /** Adresse de contact de l'auto-école, ou à défaut celle de son responsable. */
    private static String schoolContactEmail(DrivingSchoolRegistry school) {
        return school.getEmail() != null && !school.getEmail().isBlank()
                ? school.getEmail()
                : school.getAdmin().getEmail();
    }

    /** Auto-école de l'utilisateur : celle qu'il dirige, sinon sa dernière adhésion acceptée. */
    private String schoolNameOf(User user) {
        return registryRepository.findByAdmin(user).map(DrivingSchoolRegistry::getSchoolName)
                .or(() -> joinRequestRepository.findFirstByUserAndJoinStatusOrderByCreatedOnDesc(user, JoinStatus.APPROVED)
                        .flatMap(request -> registryRepository.findById(request.getDrivingSchoolId()))
                        .map(DrivingSchoolRegistry::getSchoolName))
                .orElse(null);
    }

    private static String fullName(User user) {
        return (user.getFirstname() + " " + (user.getLastname() == null ? "" : user.getLastname())).trim();
    }

    private static void add(Map<String, MailRecipient> byEmail, User user) {
        add(byEmail, user.getEmail(), user.getFirstname());
    }

    private static void add(Map<String, MailRecipient> byEmail, String email, String name) {
        if (email != null && !email.isBlank()) {
            byEmail.putIfAbsent(email.trim().toLowerCase(), new MailRecipient(email.trim().toLowerCase(), name));
        }
    }
}
