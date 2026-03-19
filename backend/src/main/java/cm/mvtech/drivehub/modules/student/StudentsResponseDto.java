package cm.mvtech.drivehub.modules.student;


import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;

public record StudentsResponseDto(
        Long id,
        User user,
        String cniRectoUrl,
        String cniVersoUrl,
        LicenseCategory licenseCategory
) {
}
