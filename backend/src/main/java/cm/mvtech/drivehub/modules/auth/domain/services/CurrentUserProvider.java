package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Accès à l'utilisateur métier connecté (élève ou moniteur) depuis les services.
 * Évite de faire passer {@code @AuthenticationPrincipal} à travers toutes les couches.
 */
@Component
public class CurrentUserProvider {

    public UserPrincipal get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new AccessDeniedException("Cette action est réservée aux élèves et moniteurs d'une auto-école");
    }

    public boolean isMonitor() {
        return get().getRole() == Role.MONITOR;
    }
}
