package cm.mvtech.drivehub.platform.admin.controllers;


import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolPendingRequestDTO;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.dto.*;
import cm.mvtech.drivehub.platform.admin.services.AdminerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
@Tag(name = "PLATFORM ADMIN API", description = "Endpoints des admins de la plateforme")
public class PlatformAdminController {

    private final AdminerService adminerService;
    private final DrivingSchoolService drivingSchoolService;

    /**
     * Login pour les admins de la plateforme (ROOT, SUPER_ADMIN, etc.)
     * PAS BESOIN de X-Tenant-ID, car ils gèrent toute la plateforme
     */
    @Operation(
            summary = "Login admin plateforme",
            description = "Ce endpoint permet aux différents ADMIN de se connecter à leur back-office si leur compte est actif"
    )
    @PostMapping("/admin/login")
    public ResponseEntity<PlatformAdminAuthResponse> adminLogin(
            @Valid @RequestBody PlatformAdminLoginRequest loginRequest) {

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(adminerService.adminerLogin(loginRequest));
    }

    @Operation(
            summary = "show admin information",
            description = "Ce endpoint permet d'afficher les informations de l'admin authentifié"
    )
    @GetMapping("/admin/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlatformAdminResponse> currentAdmin(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(adminerService.getCurrentAdmin(authentication));
    }

    /**
     * Inscription d'un admin plateforme
     * PAS BESOIN de X-Tenant-ID
     */
    @Operation(
            summary = "Inscription admin plateforme",
            description = "Endpoint d'inscription des différents administrateurs de la plateforme"
    )
    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse> adminRegister(
            @Valid @RequestBody PlatformAdminCreateRequest adminCreateRequest) {

        adminerService.adminerRegistry(adminCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Inscription réussie, en attente de validation par l'Administrateur ROOT"));
    }

    /**
     * Liste des admins en attente de validation (ROOT uniquement)
     */
    @Operation(
            summary = "Liste des admins en attente",
            description = "Ce endpoint présente la liste des administrateurs en attente ayant effectué une demande d'inscription (ROOT)"
    )
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<List<PlatformAdminResponse>> retrievePendingAdminRequests() {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(adminerService.pendingAdminerRequest());
    }

    /**
     * Activation d'un compte admin (ROOT uniquement)
     */
    @Operation(
            summary = "Activation des admins",
            description = "Ce endpoint permet au ROOT d'activer les comptes Administrateur (REVIEWER, SUPER_ADMIN)"
    )
    @PatchMapping("/admin/{adminId}/activate")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse> activateAdmin(@PathVariable UUID adminId) {
        adminerService.activateAdmin(adminId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse(true, "Compte activé avec succès"));
    }

    @Operation(
            summary = "endpoint d'approbation des moniteur et auto-ecole",
            description = "dans ce endpoint, l'on donne la possibilité au administrateur de la plateformes (REVIEWER) d'approuver les requetes de creations d'une auto-ecole et par la meme occasion d'approuver les moniteurs de celle-ci"
    )
    @PatchMapping("/registries/{registryId}/approve")
    @PreAuthorize("hasRole('REVIEWER') || hasRole('ROOT')")
    public ResponseEntity<ApiResponse> approve(@PathVariable UUID registryId) {
        drivingSchoolService.approveRegistry(registryId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse(true, "requête approuvé avec success"));
    }

    @Operation(
            summary = "requete de creation d'auto-école en attente",
            description = "dans ce endpoint, l'on donne la possibilité au administrateur de la plateformes (ROOT/admin) de consulter l'ensemble des requetes de creation d'auto école en attentes"
    )
    @GetMapping("/registries/pending")
    @PreAuthorize("hasRole('REVIEWER') || hasRole('ROOT')")
    public ResponseEntity<List<DrivingSchoolPendingRequestDTO>> retrievePendingRequest() {
        return ResponseEntity.status(200).body(drivingSchoolService.retreivePendingRequest());
    }

    @Operation(
            summary = "Liste tous les admins",
            description = "Pagination + filtres optionnels par statut et rôle. ROOT uniquement."
    )
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<Page<PlatformAdminResponse>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AdminStatus status,
            @RequestParam(required = false) AdminRole role) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminerService.getAllAdmins(pageable, status, role));
    }

    @Operation(
            summary = "Détail d'un admin",
            description = "Retourne les informations complètes d'un admin par son ID."
    )
    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<PlatformAdminResponse> getAdminById(
            @PathVariable UUID adminId) {
        return ResponseEntity.ok(adminerService.getAdminById(adminId));
    }

    @Operation(
            summary = "Modifier un admin",
            description = "Modifie les informations d'un admin (nom, email, rôle). ROOT uniquement."
    )
    @PatchMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<PlatformAdminResponse> updateAdmin(
            @PathVariable UUID adminId,
            @Valid @RequestBody UpdateAdminRequest request) {
        return ResponseEntity.ok(adminerService.updateAdmin(adminId, request));
    }

    @Operation(
            summary = "Désactiver un admin",
            description = "Suspend temporairement un admin. Réversible. ROOT uniquement."
    )
    @PatchMapping("/admin/{adminId}/deactivate")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse> deactivateAdmin(@PathVariable UUID adminId) {
        adminerService.deactivateAdmin(adminId);
        return ResponseEntity.ok(new ApiResponse(true, "Admin suspendu avec succès"));
    }

    @Operation(
            summary = "Supprimer un admin",
            description = "Supprime définitivement un compte admin. ROOT uniquement."
    )
    @DeleteMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse> deleteAdmin(@PathVariable UUID adminId) {
        adminerService.deleteAdmin(adminId);
        return ResponseEntity.ok(new ApiResponse(true, "Admin supprimé avec succès"));
    }

    @Operation(
            summary = "Modifier mon profil",
            description = "L'admin connecté modifie ses propres informations."
    )
    @PatchMapping("/admin/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlatformAdminResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateAdminRequest request) {
        return ResponseEntity.ok(adminerService.updateMyProfile(authentication, request));
    }

    @Operation(
            summary = "Changer mon mot de passe",
            description = "L'admin connecté change son mot de passe."
    )
    @PatchMapping("/admin/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> changeMyPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        adminerService.changeMyPassword(authentication, request);
        return ResponseEntity.ok(
                new ApiResponse(true, "Mot de passe modifié avec succès"));
    }

    @Operation(
            summary = "Statistiques des admins",
            description = "Chiffres clés pour le dashboard : total, pending, active, inactive."
    )
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ROOT') || hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        return ResponseEntity.ok(adminerService.getAdminStats());
    }

}