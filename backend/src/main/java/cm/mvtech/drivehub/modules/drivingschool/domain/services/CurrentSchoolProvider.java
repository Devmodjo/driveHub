package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRepository;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Auto-école du tenant courant.
 *
 * <p>Dans l'architecture "un schéma par auto-école", le schéma sélectionné par X-Tenant-ID
 * contient UNE seule ligne driving_school. Les requêtes métier n'ont donc plus besoin
 * d'envoyer un drivingSchoolId : l'auto-école est implicite.</p>
 */
@Component
@RequiredArgsConstructor
public class CurrentSchoolProvider {

    private final DrivingSchoolRepository drivingSchoolRepository;

    public DrivingSchool get() {
        return drivingSchoolRepository.findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Auto-école introuvable pour ce tenant"));
    }
}
