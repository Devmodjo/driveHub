package cm.mvtech.drivehub.modules.exam.application.dto;

import cm.mvtech.drivehub.modules.enums.InscriptionStatus;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExamsInscriptionResponseDto(
        UUID id,
        UUID examId,
        LocalDateTime dateExams,
        LicenseCategory category,
        UUID studentId,
        String studentFirstname,
        String studentLastname,
        InscriptionStatus inscriptionStatus,
        LocalDate registeredAt
) {
}
