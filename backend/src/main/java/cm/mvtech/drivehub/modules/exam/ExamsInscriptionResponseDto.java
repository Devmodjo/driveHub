package cm.mvtech.drivehub.modules.exam;

import cm.mvtech.drivehub.modules.enums.InscriptionStatus;
import cm.mvtech.drivehub.modules.student.StudentsResponseDto;

import java.time.LocalDate;
import java.util.UUID;

public record ExamsInscriptionResponseDto(
        UUID id,
        ExamsResponseDto exams,
        StudentsResponseDto students,
        InscriptionStatus inscriptionStatus,
        LocalDate registeredAt
) {
}
