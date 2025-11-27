package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.LicenseCategory;

import java.util.Date;

public record ExamsResponseDto(
        Long id,
        DrivingSchoolResponseDto drivingSchool,
        Date dateExams,
        LicenseCategory category
) {
}
