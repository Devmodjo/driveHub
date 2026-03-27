package cm.mvtech.drivehub.modules.exam;

import cm.mvtech.drivehub.modules.enums.InscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ExamsInscriptionRequestDto(

        @Positive
        @NotBlank(message = "l'identifiant de l'exament est obligatoire")
        UUID examId,

        @Positive
        @NotBlank(message = "l'identifiant de l'étudiant est obligatoire")
        UUID studentId,

        @NotBlank(message = "le status est l'inscript est obligatoire")
        InscriptionStatus inscriptionStatus
) {
}
