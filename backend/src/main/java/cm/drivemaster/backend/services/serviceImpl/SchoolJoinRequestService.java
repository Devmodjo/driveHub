package cm.drivemaster.backend.services.serviceImpl;


import cm.drivemaster.backend.beans.SchoolJoinRequest;
import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.enums.JoinStatus;
import cm.drivemaster.backend.enums.ProfileStatus;
import cm.drivemaster.backend.models.dto.JoinSchoolRequestDto;

import cm.drivemaster.backend.repositories.SchoolJoinRequestRepository;
import cm.drivemaster.backend.repositories.UserRepository;
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
