package cm.drivemaster.backend.services;

import cm.drivemaster.backend.models.dto.*;

import java.util.List;

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
     * Activer un compte admin
     * Accessible uniquement par ROOT
     * @param adminId ID de l'admin à activer
     */
    void activateAdmin(long adminId);
}