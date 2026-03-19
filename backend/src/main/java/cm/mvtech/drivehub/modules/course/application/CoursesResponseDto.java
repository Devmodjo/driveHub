package cm.mvtech.drivehub.modules.course.application;

import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;

public record CoursesResponseDto (
        Long id,
        String title,
        String content,
        DrivingSchoolResponseDto drivingSchool
){
}
