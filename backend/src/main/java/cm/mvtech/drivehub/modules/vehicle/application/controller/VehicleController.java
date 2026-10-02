package cm.mvtech.drivehub.modules.vehicle.application.controller;

import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesRequestDto;
import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesResponseDto;
import cm.mvtech.drivehub.modules.vehicle.domain.services.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Flotte de l'auto-école (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "VEHICLE API", description = "gestion de la flotte de l'auto-école (tenant)")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "liste paginée des véhicules")
    @GetMapping
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public ApiPageResponse<VehiclesResponseDto> list(@PageableDefault(size = 20, sort = "matriculation") Pageable pageable) {
        return vehicleService.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public VehiclesResponseDto get(@PathVariable UUID id) {
        return vehicleService.get(id);
    }

    @Operation(summary = "ajouter un véhicule")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MONITOR')")
    public VehiclesResponseDto create(@Valid @RequestBody VehiclesRequestDto request) {
        return vehicleService.create(request);
    }

    @Operation(summary = "modifier un véhicule (dont son état : disponible, en panne, en maintenance)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public VehiclesResponseDto update(@PathVariable UUID id, @Valid @RequestBody VehiclesRequestDto request) {
        return vehicleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public ApiResponse delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return new ApiResponse(true, "Véhicule supprimé");
    }
}
