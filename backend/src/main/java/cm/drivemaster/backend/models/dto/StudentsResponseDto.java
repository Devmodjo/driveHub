package cm.drivemaster.backend.models.dto;


import cm.drivemaster.backend.beans.Users;
import cm.drivemaster.backend.enums.LicenseCategory;

public record StudentsResponseDto(
        Long id,
        Users users,
        String cniRectoUrl,
        String cniVersoUrl,
        LicenseCategory licenseCategory
) {
}
