package cm.drivemaster.backend.models.dto;


public record MonitorResponseDto(
        Long id,
        UserResponseDto users,
        DrivingSchoolResponseDto drivingSchoolId,
        String phoneNumber
) {
}
