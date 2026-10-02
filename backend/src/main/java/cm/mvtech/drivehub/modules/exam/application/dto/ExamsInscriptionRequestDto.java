package cm.mvtech.drivehub.modules.exam.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Inscription d'un élève à un examen (le statut est fixé par le service : INSCRIT). */
public record ExamsInscriptionRequestDto(
        @NotNull(message = "l'identifiant de l'étudiant est obligatoire")
        UUID studentId
) {
}
