package cm.drivemaster.backend.controllers;


import cm.drivemaster.backend.models.dto.ApiResponse;
import cm.drivemaster.backend.models.dto.DrivingSchoolRequestDto;
import cm.drivemaster.backend.models.dto.DrivingSchoolResponseDto;
import cm.drivemaster.backend.services.DrivingSchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse> createSchool(@RequestBody DrivingSchoolRequestDto dto, long adminId) throws IllegalAccessException {
        drivingSchoolService.createSchool(dto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Auto ecole enregistrez en attentes de validations par les admin"));
    }

    @Operation(
            summary = "endpoint d'approbation des moniteur et auto-ecole",
            description = "dans ce endpoint, l'on donne la possibilité au administrateur de la plateformes (ROOT/admin) d'approuver les requetes de creations d'une auto-ecole et par la meme occasion d'approuver les moniteurs de celle-ci"
    )
    @GetMapping("{registryId}/approve")
    @PreAuthorize("hasRole('REVIEWER', 'ROOT')")
    public ResponseEntity<ApiResponse> approve(@PathVariable long registryId) {
        drivingSchoolService.approveRegistry(registryId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse(true, "requête approuvé avec success"));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DrivingSchoolResponseDto>> retreiveSchool() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(drivingSchoolService.retreiveSchool());
    }
}
