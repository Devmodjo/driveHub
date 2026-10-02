package cm.mvtech.drivehub.modules.student.application.dto;

import cm.mvtech.drivehub.modules.enums.Gender;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Élève renvoyé par l'API.
 * Correction : l'ancienne version exposait l'entité {@code User} complète (mot de passe haché compris).
 */
public record StudentsResponseDto(
        UUID id,
        UUID userId,
        String firstname,
        String lastname,
        String email,
        String phoneNumber,
        Date dateOfBirth,
        Gender gender,
        String nationality,
        String residenceCity,
        String cniRectoUrl,
        String cniVersoUrl,
        LicenseCategory licenseCategory,
        LocalDateTime createdOn
) {
}
