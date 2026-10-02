package cm.mvtech.drivehub.modules.reservation.application.dto;

import cm.mvtech.drivehub.modules.enums.ReservationTypes;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Demande de réservation d'un créneau.
 *
 * @param studentId obligatoire si c'est le moniteur qui réserve ; ignoré si c'est l'élève (c'est lui-même)
 * @param vehicleId obligatoire pour une leçon de CONDUITE
 */
public record ReservationsRequestDto(
        UUID studentId,
        @NotNull(message = "l'identifiant de l'encadreur est obligatoire")
        UUID monitorId,
        UUID vehicleId,
        @NotNull(message = "la date du créneau est obligatoire")
        @Future(message = "le créneau doit être dans le futur")
        LocalDateTime dateTime,
        @NotNull(message = "le type de reservation est obligatoire")
        ReservationTypes types
) {
}
