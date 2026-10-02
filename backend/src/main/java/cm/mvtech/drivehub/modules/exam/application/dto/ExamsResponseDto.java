package cm.mvtech.drivehub.modules.exam.application.dto;

import cm.mvtech.drivehub.modules.enums.LicenseCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExamsResponseDto(
        UUID id,
        LocalDateTime dateExams,
        LicenseCategory category
) {
}
