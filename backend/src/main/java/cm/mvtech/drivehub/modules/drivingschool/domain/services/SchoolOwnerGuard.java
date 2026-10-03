package cm.mvtech.drivehub.modules.drivingschool.domain.services;

import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contrôle réservé au RESPONSABLE de l'auto-école (le moniteur qui l'a créée), par exemple pour ajouter
 * un moniteur. Les moniteurs salariés n'ont pas ce droit.
 */
@Service
@RequiredArgsConstructor
public class SchoolOwnerGuard {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final DrivingSchoolRegistryRepository registryRepository;

    /**
     * Le moniteur connecté doit être le responsable de l'auto-école du tenant courant (X-Tenant-ID).
     *
     * @return le compte du responsable
     */
    @Transactional(readOnly = true)
    public User requireOwnerOfCurrentSchool() {
        User user = userRepository.findById(currentUserProvider.get().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        String tenant = TenantContext.getTenantId();
        boolean owner = registryRepository.findByAdmin(user)
                .map(DrivingSchoolRegistry::getSchemaName)
                .filter(schema -> schema.equals(tenant))
                .isPresent();
        if (!owner) {
            throw new AccessDeniedException("Seul le responsable de l'auto-école peut faire cette action");
        }
        return user;
    }
}
