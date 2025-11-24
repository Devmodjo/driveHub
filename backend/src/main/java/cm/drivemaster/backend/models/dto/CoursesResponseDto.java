package cm.drivemaster.backend.models.dto;

public record CoursesResponseDto (
        Long id,
        String title,
        String content,
        DrivingSchoolResponseDto drivingSchool
){
}
