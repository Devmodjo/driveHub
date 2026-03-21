package cm.mvtech.drivehub.modules.exam;

import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;

import java.util.Date;
import java.util.UUID;

public record ExamsResponseDto(
        UUID id,
        DrivingSchoolResponseDto drivingSchool,
        Date dateExams,
        LicenseCategory category
) {
}
