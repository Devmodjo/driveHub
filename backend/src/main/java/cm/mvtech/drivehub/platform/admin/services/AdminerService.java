package cm.mvtech.drivehub.platform.admin.services;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    /** Lister tous les admins avec pagination et filtre optionnel par statut/rôle */
    Page<PlatformAdminResponse> getAllAdmins(Pageable pageable,
                                             AdminStatus status,
                                             AdminRole role);

    /** Détail d'un admin par son ID */
    PlatformAdminResponse getAdminById(UUID adminId);

    /** Modifier les informations d'un admin */
    PlatformAdminResponse updateAdmin(UUID adminId, UpdateAdminRequest request);

    /** Désactiver un admin (suspension réversible) */
    void deactivateAdmin(UUID adminId);

    /** Supprimer définitivement un admin */
    void deleteAdmin(UUID adminId);

    /** L'admin connecté modifie son propre profil */
    PlatformAdminResponse updateMyProfile(Authentication authentication,
                                          UpdateAdminRequest request);

    /** Changer son propre mot de passe */
    void changeMyPassword(Authentication authentication,
                          ChangePasswordRequest request);

    /** Stats dashboard admins */
    AdminStatsResponse getAdminStats();



}