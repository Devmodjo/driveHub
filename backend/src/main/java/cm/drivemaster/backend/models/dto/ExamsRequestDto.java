package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.LicenseCategory;

import java.util.Date;

public record ExamsRequestDto(
        Long drivingSchoolId,
        Date dateExams,
        LicenseCategory category

) {
}
