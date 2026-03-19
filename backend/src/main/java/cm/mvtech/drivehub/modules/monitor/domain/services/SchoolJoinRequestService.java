package cm.mvtech.drivehub.modules.monitor.domain.services;


import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.messageapi.JoinSchoolRequestDto;

import cm.mvtech.drivehub.modules.monitor.infrastucture.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolJoinRequestService {

    private final SchoolJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    /**
     * icic on enregistrer les requete permettant aux
     * utilisateur de rejoindre une auto-ecole
     * @param email id de l'utilisateur
     * @param dto information specifique de la requête
     */
    @Transactional
    public void requestJoin(
            String email,
            JoinSchoolRequestDto dto
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User introuvable"));

//        if (user.getProfileStatus() != ProfileStatus.ACTIVE) {
//            throw new IllegalStateException("Profil non actif");
//        }

        if (user.getProfileStatus() != ProfileStatus.REGISTERED) {
            throw new IllegalStateException(
                    "Seuls les utilisateurs enregistrés peuvent demander une adhésion"
            );
        }

        SchoolJoinRequest request = new SchoolJoinRequest();
        request.setUser(user);
        request.setDrivingSchoolId(dto.drivingSchoolId());
        request.setRequestedRole(dto.role());
        request.setJoinStatus(JoinStatus.PENDING);

        joinRequestRepository.save(request);
    }

}
