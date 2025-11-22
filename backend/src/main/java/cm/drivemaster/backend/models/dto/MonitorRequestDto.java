package cm.drivemaster.backend.models.dto;

public record MonitorRequestDto(
        Long userId,
        Long drivingSchoolId,
        String phoneNumber
) {
}
