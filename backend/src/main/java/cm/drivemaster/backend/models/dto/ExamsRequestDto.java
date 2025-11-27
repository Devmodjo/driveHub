package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.LicenseCategory;
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
