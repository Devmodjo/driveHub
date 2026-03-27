package cm.mvtech.drivehub.modules.course.application;

import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;

import java.util.UUID;

public record CoursesResponseDto (
        UUID id,
        String title,
        String content,
        DrivingSchoolResponseDto drivingSchool
){
}
