package cm.mvtech.drivehub.modules.monitor.application.dto;


import cm.mvtech.drivehub.modules.auth.application.dto.UserResponseDto;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;

import java.util.UUID;

public record MonitorResponseDto(
        UUID id,
        UserResponseDto users,
        DrivingSchoolResponseDto drivingSchoolId,
        String phoneNumber
) {
}
