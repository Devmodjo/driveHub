package cm.mvtech.drivehub.modules.student;


import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;

import java.util.UUID;

public record StudentsResponseDto(
        UUID id,
        User user,
        String cniRectoUrl,
        String cniVersoUrl,
        LicenseCategory licenseCategory
) {
}
