package cm.mvtech.drivehub.modules.exam;

import cm.mvtech.drivehub.modules.enums.InscriptionStatus;
import cm.mvtech.drivehub.modules.student.StudentsResponseDto;

import java.time.LocalDate;

public record ExamsInscriptionResponseDto(
        Long id,
        ExamsResponseDto exams,
        StudentsResponseDto students,
        InscriptionStatus inscriptionStatus,
        LocalDate registeredAt
) {
}
