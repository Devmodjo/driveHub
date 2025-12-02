package cm.drivemaster.backend.models.dto;


import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.enums.LicenseCategory;

public record StudentsResponseDto(
        Long id,
        User user,
        String cniRectoUrl,
        String cniVersoUrl,
        LicenseCategory licenseCategory
) {
}
