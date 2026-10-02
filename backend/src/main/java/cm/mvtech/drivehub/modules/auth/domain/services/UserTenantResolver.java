package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * Détermine, côté SERVEUR, à quelle auto-école (schéma tenant) appartient un utilisateur.
 *
 * <p>Le résultat est inscrit dans le JWT à la connexion. C'est la seule source fiable du tenant :
 * auparavant, le JWT contenait toujours "public" et le tenant venait de l'en-tête X-Tenant-ID,
 * choisi librement par le client.</p>
 */
@Service
@RequiredArgsConstructor
public class UserTenantResolver {

    private static final Set<DrivingSchoolStatus> OPERATIONAL =
            Set.of(DrivingSchoolStatus.APPROVED, DrivingSchoolStatus.ACTIVE);

    private final DrivingSchoolRegistryRepository registryRepository;
    private final SchoolJoinRequestRepository joinRequestRepository;

    /** Schéma de l'auto-école de l'utilisateur, ou vide s'il n'en a pas encore. */
    public Optional<String> resolveTenant(User user) {
        // 1. Moniteur fondateur : l'auto-école dont il est l'administrateur.
        Optional<String> owned = registryRepository.findByAdmin(user)
                .filter(registry -> OPERATIONAL.contains(registry.getDrivingSchoolStatus()))
                .map(DrivingSchoolRegistry::getSchemaName);
        if (owned.isPresent()) {
            return owned;
        }

        // 2. Élève ou moniteur rattaché : sa dernière demande d'adhésion acceptée.
        return joinRequestRepository.findFirstByUserAndJoinStatusOrderByCreatedOnDesc(user, JoinStatus.APPROVED)
                .flatMap(request -> registryRepository.findById(request.getDrivingSchoolId()))
                .filter(registry -> OPERATIONAL.contains(registry.getDrivingSchoolStatus()))
                .map(DrivingSchoolRegistry::getSchemaName);
    }
}
