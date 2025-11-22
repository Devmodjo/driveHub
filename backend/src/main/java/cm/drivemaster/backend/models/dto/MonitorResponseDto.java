package cm.drivemaster.backend.models.dto;

public record MonitorResponseDto(
        Long id,
        Long userId,
        Long drivingSchoolId,
        String phoneNumber
) {
}
