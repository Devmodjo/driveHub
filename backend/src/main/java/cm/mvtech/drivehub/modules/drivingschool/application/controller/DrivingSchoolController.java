package cm.mvtech.drivehub.modules.drivingschool.application.controller;


import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolPendingRequestDTO;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolRequestDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/driving-schools")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
@Tag(name="DRIVING SCHOOL API", description = "api pour la gestion métier des auto-écoles")
public class DrivingSchoolController {

    private final DrivingSchoolService drivingSchoolService;

    @Operation(
            summary = "endpoint de création d'une auto école",
            description = "ce endpoint cree un requeste de creation d'une auto-ecole qui doit être validé par les admins de la plateforme. Seul les Moniteur peuvent effectuer ce type de requête"
    )
    @PostMapping("/request")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<ApiResponse> createSchool(
            @RequestBody DrivingSchoolRequestDto dto,
            @AuthenticationPrincipal UserPrincipal user) throws IllegalAccessException {

        if (user == null) {

            log.error("UserPrincipal est null dans l'endpoint /request");
            log.error("SecurityContext Authentication = {}", SecurityContextHolder.getContext().getAuthentication());

            return ResponseEntity.status(403).body(new ApiResponse(false, "Utilisateur principal non trouvé"));
        }

        log.info("UserPrincipal trouvé : id={}, email={}", user.getId(), user.getUsername());
        drivingSchoolService.createSchool(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Auto ecole enregistrée en attente de validation"));
    }

    @Operation(
            summary = "endpoint d'approbation des moniteur et auto-ecole",
            description = "dans ce endpoint, l'on donne la possibilité au administrateur de la plateformes (ROOT/admin) d'approuver les requetes de creations d'une auto-ecole et par la meme occasion d'approuver les moniteurs de celle-ci"
    )
    @GetMapping("{registryId}/approve")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<ApiResponse> approve(@PathVariable long registryId) {
        drivingSchoolService.approveRegistry(registryId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse(true, "requête approuvé avec success"));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DrivingSchoolResponseDto>> retreiveSchool() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(drivingSchoolService.retreiveSchool());
    }

    @Operation(
            summary = "requete en attente",
            description = "dans ce endpoint, l'on donne la possibilité au administrateur de la plateformes (ROOT/admin) de consulter l'ensemble des requetes de creation d'auto école en attentes"
    )
    @GetMapping("/request/pending")
    @PreAuthorize("hasRole('REVIEWER', 'ROOT')")
    public ResponseEntity<List<DrivingSchoolPendingRequestDTO>> retrievePendingRequest() {
        return ResponseEntity.status(200).body(drivingSchoolService.retreivePendingRequest());
    }
}
