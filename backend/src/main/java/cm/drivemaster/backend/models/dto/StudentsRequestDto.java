package cm.drivemaster.backend.models.dto;


import cm.drivemaster.backend.enums.LicenseCategory;

public record StudentsRequestDto(
        Long userId,
        String cniRectoUrl,
        String cniVersoUrl,
        LicenseCategory licenseCategory
) {
}
