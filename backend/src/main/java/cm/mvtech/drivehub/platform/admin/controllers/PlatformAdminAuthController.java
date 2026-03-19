package cm.mvtech.drivehub.platform.admin.controllers;


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

@RestController
@RequestMapping("/api/platform/admin")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
@Tag(name = "PLATFORM ADMIN API", description = "Endpoints d'authentification des admins de la plateforme")
public class PlatformAdminAuthController {

    private final AdminerService adminerService;

    /**
     * Login pour les admins de la plateforme (ROOT, SUPER_ADMIN, etc.)
     * PAS BESOIN de X-Tenant-ID car ils gèrent toute la plateforme
     */
    @Operation(
            summary = "Login admin plateforme",
            description = "Ce endpoint permet aux différents ADMIN de se connecter à leur back-office si leur compte est actif"
    )
    @PostMapping("/login")
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
    @PostMapping("/register")
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
    @GetMapping("/pending")
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
            description = "Ce endpoint permet au ROOT d'activer les comptes utilisateurs"
    )
    @GetMapping("{adminId}/activate")
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<ApiResponse> activateAdmin(@PathVariable long adminId) {
        adminerService.activateAdmin(adminId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse(true, "Compte activé avec succès"));
    }
}