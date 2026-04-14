package cm.mvtech.drivehub.platform.admin.services;

import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminAuthResponse;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminCreateRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminLoginRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

/**
 * Service d'administration de la plateforme
 * Pour les admins ROOT, SUPER_ADMIN, etc.
 * Ces admins gèrent la plateforme globale, PAS les tenants individuels
 */
public interface AdminerService {

    /**
     * Authentification d'un admin plateforme
     * @param loginRequest Informations de connexion
     * @return Response avec JWT token PLATFORM_ADMIN
     */
    PlatformAdminAuthResponse adminerLogin(PlatformAdminLoginRequest loginRequest);

    /**
     * Inscription d'un nouvel admin plateforme
     * Le compte sera PENDING jusqu'à validation par un ROOT
     * @param adminCreateRequest Informations d'inscription
     */
    void adminerRegistry(PlatformAdminCreateRequest adminCreateRequest);

    /**
     * Liste des admins en attente de validation
     * Accessible uniquement par ROOT
     * @return Liste des admins PENDING
     */
    List<PlatformAdminResponse> pendingAdminerRequest();

    /**
     * afficher l'admin authentifier
     * @param authentication identifie celui qui s'est connecté
     * @return l'info de l'admin
     */
    PlatformAdminResponse getCurrentAdmin(Authentication authentication);

    /**
     * Activer un compte admin
     * Accessible uniquement par ROOT
     * @param adminId ID de l'admin à activer
     */
    void activateAdmin(UUID adminId);
}