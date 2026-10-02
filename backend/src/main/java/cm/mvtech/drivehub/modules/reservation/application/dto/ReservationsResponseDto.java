package cm.mvtech.drivehub.modules.reservation.application.dto;

import cm.mvtech.drivehub.modules.enums.ReservationStatus;
import cm.mvtech.drivehub.modules.enums.ReservationTypes;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationsResponseDto(
        UUID id,
        UUID studentId,
        String studentFirstname,
        String studentLastname,
        UUID monitorId,
        String monitorFirstname,
        String monitorLastname,
        UUID vehicleId,
        String vehicleMatriculation,
        LocalDateTime dateTime,
        ReservationTypes types,
        ReservationStatus reservationStatus
) {
}
