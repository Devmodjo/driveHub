package cm.drivemaster.backend.models.dto;

public record CoursesRequestDto(
        Long drivingSchoolId,
        String title,
        String content
) {
}
