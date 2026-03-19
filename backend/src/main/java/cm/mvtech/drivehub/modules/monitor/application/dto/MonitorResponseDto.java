package cm.mvtech.drivehub.modules.monitor.application.dto;


import cm.mvtech.drivehub.modules.auth.application.dto.UserResponseDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;

public record MonitorResponseDto(
        Long id,
        UserResponseDto users,
        DrivingSchoolResponseDto drivingSchoolId,
        String phoneNumber
) {
}
