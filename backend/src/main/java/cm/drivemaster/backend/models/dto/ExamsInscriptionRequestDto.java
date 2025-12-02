package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.InscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ExamsInscriptionRequestDto(

        @Positive
        @NotBlank(message = "l'identifiant de l'exament est obligatoire")
        Long examId,

        @Positive
        @NotBlank(message = "l'identifiant de l'étudiant est obligatoire")
        Long studentId,

        @NotBlank(message = "le status est l'inscript est obligatoire")
        InscriptionStatus inscriptionStatus
) {
}
