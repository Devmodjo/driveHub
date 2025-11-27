package cm.drivemaster.backend.models.dto;


import cm.drivemaster.backend.enums.LicenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StudentsRequestDto(
        @Positive
        @NotBlank(message = "l'identifiant de l'utilisateur est obligatoire")
        Long userId,

        @NotBlank(message = "l'url de la CNI (rector) est obligatoire")
        String cniRectoUrl,

        @NotBlank(message = "l'url de la CNI (verso) est obligatoire")
        String cniVersoUrl,

        @NotBlank(message = "la categorie du permis est obligatoire")
        LicenseCategory licenseCategory
) {
}
