package cm.mvtech.drivehub.modules.exam;

import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Date;

public record ExamsRequestDto(
        @Positive
        @NotBlank(message = "l'identifiant de l'exament est obligatoire")
        Long drivingSchoolId,

        @NotBlank(message = "la date de l'examen est obligatoire")
        Date dateExams,

        @NotBlank(message = "la categorie de l'examen est obligaoire")
        LicenseCategory category

) {
}
