package cm.mvtech.drivehub.modules.reservation.application.controller;

import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.reservation.application.dto.ReservationsRequestDto;
import cm.mvtech.drivehub.modules.reservation.application.dto.ReservationsResponseDto;
import cm.mvtech.drivehub.modules.reservation.domain.services.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Planning des créneaux (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "RESERVATION API", description = "réservation des leçons et rendez-vous (tenant)")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "réserver un créneau (élève : en attente ; moniteur : confirmé)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public ReservationsResponseDto create(@Valid @RequestBody ReservationsRequestDto request) {
        return reservationService.create(request);
    }

    @Operation(summary = "réservations (moniteur : toutes ; élève : les siennes)")
    @GetMapping
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public ApiPageResponse<ReservationsResponseDto> list(
            @PageableDefault(size = 20, sort = "dateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return reservationService.list(pageable);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('MONITOR')")
    public ReservationsResponseDto confirm(@PathVariable UUID id) {
        return reservationService.confirm(id);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public ReservationsResponseDto cancel(@PathVariable UUID id) {
        return reservationService.cancel(id);
    }
}
