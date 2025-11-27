package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.InscriptionStatus;

import java.time.LocalDate;

public record ExamsInscriptionResponseDto(
        Long id,
        ExamsResponseDto exams,
        StudentsResponseDto students,
        InscriptionStatus status,
        LocalDate registeredAt
) {
}
