package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.InscriptionStatus;

public record ExamsInscriptionRequestDto(
        Long examId,
        Long studentId,
        InscriptionStatus status
) {
}
