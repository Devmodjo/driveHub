package cm.mvtech.drivehub.platform.admin.controllers;


import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolPendingRequestDTO;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminAuthResponse;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminCreateRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminLoginRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminResponse;
import cm.mvtech.drivehub.platform.admin.services.AdminerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            description = "Ce endpoint permet au ROOT d'activer les comptes utilisateurs (Moniteur)"
    )
    @GetMapping("/admin/{adminId}/activate")
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
    @GetMapping("/registries/{registryId}/approve")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<ApiResponse> approve(@PathVariable UUID registryId) {
        drivingSchoolService.approveRegistry(registryId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse(true, "requête approuvé avec success"));
    }

    @Operation(
            summary = "requete en attente",
            description = "dans ce endpoint, l'on donne la possibilité au administrateur de la plateformes (ROOT/admin) de consulter l'ensemble des requetes de creation d'auto école en attentes"
    )
    @GetMapping("/registries/pending")
    @PreAuthorize("hasRole('REVIEWER', 'ROOT')")
    public ResponseEntity<List<DrivingSchoolPendingRequestDTO>> retrievePendingRequest() {
        return ResponseEntity.status(200).body(drivingSchoolService.retreivePendingRequest());
    }

}