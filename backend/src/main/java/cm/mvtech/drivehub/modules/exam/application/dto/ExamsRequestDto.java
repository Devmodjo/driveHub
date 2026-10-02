package cm.mvtech.drivehub.modules.exam.application.dto;

import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Création / modification d'une session d'examen. */
public record ExamsRequestDto(
        @NotNull(message = "la date de l'examen est obligatoire")
        @Future(message = "la date de l'examen doit être dans le futur")
        LocalDateTime dateExams,
        @NotNull(message = "la categorie de l'examen est obligatoire")
        LicenseCategory category
) {
}
