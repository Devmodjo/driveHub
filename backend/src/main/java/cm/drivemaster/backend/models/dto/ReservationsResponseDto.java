package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.ReservationStatus;
import cm.drivemaster.backend.enums.ReservationTypes;

import java.time.LocalDateTime;

public record ReservationsResponseDto(
        Long id,
        DrivingSchoolResponseDto drivingSchool,
        StudentsResponseDto student,
        MonitorResponseDto monitor,
        LocalDateTime dateTime,
        ReservationTypes types,
        ReservationStatus status
) {
}
