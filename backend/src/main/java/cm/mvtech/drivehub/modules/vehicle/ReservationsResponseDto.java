package cm.mvtech.drivehub.modules.vehicle;

import cm.mvtech.drivehub.modules.enums.ReservationStatus;
import cm.mvtech.drivehub.modules.enums.ReservationTypes;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import cm.mvtech.drivehub.modules.student.StudentsResponseDto;

import java.time.LocalDateTime;

public record ReservationsResponseDto(
        Long id,
        DrivingSchoolResponseDto drivingSchool,
        StudentsResponseDto student,
        MonitorResponseDto monitor,
        LocalDateTime dateTime,
        ReservationTypes types,
        ReservationStatus reservationStatus
) {
}
