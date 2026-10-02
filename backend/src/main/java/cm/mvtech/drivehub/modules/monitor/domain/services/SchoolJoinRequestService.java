package cm.mvtech.drivehub.modules.monitor.domain.services;

import cm.mvtech.drivehub.modules.document.domain.services.DocumentService;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.JoinSchoolRequestDto;
import cm.mvtech.drivehub.modules.messageapi.MyJoinRequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SchoolJoinRequestService {

    private static final Set<DrivingSchoolStatus> OPEN_SCHOOLS =
            Set.of(DrivingSchoolStatus.APPROVED, DrivingSchoolStatus.ACTIVE);

    private final SchoolJoinRequestRepository joinRequestRepository;
    private final DrivingSchoolRegistryRepository registryRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final DocumentService documentService;

    /**
     * icic on enregistrer les requete permettant aux
     * utilisateur de rejoindre une auto-ecole
     *
     * <p>Corrections :</p>
     * <ul>
     *   <li>le statut exigé était REGISTERED alors que les élèves étaient créés ACTIVE et que les
     *       moniteurs passent EMAIL_VERIFIED après vérification : personne ne pouvait faire de demande.
     *       On exige maintenant un email vérifié ;</li>
     *   <li>le rôle demandé est celui du compte (un élève ne peut pas demander à devenir moniteur) ;</li>
     *   <li>l'auto-école doit exister et être validée ; pas de double demande en attente.</li>
     * </ul>
     *
     * @param email id de l'utilisateur
     * @param dto   information specifique de la requête
     */
    @Transactional
    public void requestJoin(String email, JoinSchoolRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User introuvable"));

        if (user.getProfileStatus() != ProfileStatus.EMAIL_VERIFIED) {
            throw new BadRequestException(
                    "Votre email doit être vérifié et vous ne devez pas déjà appartenir à une auto-école");
        }
        if (dto.role() != null && dto.role() != user.getRoles()) {
            throw new BadRequestException("Le rôle demandé ne correspond pas à votre compte");
        }
        if (dto.drivingSchoolId() == null) {
            throw new BadRequestException("L'identifiant de l'auto-école est obligatoire");
        }

        DrivingSchoolRegistry registry = registryRepository.findById(dto.drivingSchoolId())
                .filter(r -> OPEN_SCHOOLS.contains(r.getDrivingSchoolStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Auto-école introuvable ou non validée"));

        if (joinRequestRepository.existsByUserAndDrivingSchoolIdAndJoinStatus(user, registry.getId(), JoinStatus.PENDING)) {
            throw new ConflictException("Une demande est déjà en attente pour cette auto-école");
        }
        // Élève : pièce d'identité ; moniteur : pièce d'identité et CAPEC (vérifiés par le responsable)
        documentService.assertRequiredDocuments(user, "avant de demander à rejoindre une auto-école");

        SchoolJoinRequest request = new SchoolJoinRequest();
        request.setUser(user);
        request.setDrivingSchoolId(registry.getId());   // id du REGISTRE (schéma public)
        request.setRequestedRole(user.getRoles());
        request.setJoinStatus(JoinStatus.PENDING);
        joinRequestRepository.save(request);

        // Prévenir le responsable de l'auto-école qu'une demande l'attend
        User owner = registry.getAdmin();
        String applicant = (user.getFirstname() + " " + (user.getLastname() == null ? "" : user.getLastname())).trim();
        emailService.sendJoinRequestReceivedEmail(owner.getEmail(), owner.getFirstname(), applicant, registry.getSchoolName());
    }

    /** Demandes d'adhésion de l'utilisateur connecté, avec le nom de l'auto-école. */
    @Transactional(readOnly = true)
    public java.util.List<MyJoinRequestResponse> myRequests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User introuvable"));
        return joinRequestRepository.findAllByUserOrderByCreatedOnDesc(user).stream()
                .map(request -> {
                    var registry = registryRepository.findById(request.getDrivingSchoolId()).orElse(null);
                    return new MyJoinRequestResponse(
                            request.getId(),
                            request.getDrivingSchoolId(),
                            registry == null ? null : registry.getSchoolName(),
                            registry == null ? null : registry.getCity(),
                            request.getJoinStatus(),
                            request.getCreatedOn());
                })
                .toList();
    }
}
