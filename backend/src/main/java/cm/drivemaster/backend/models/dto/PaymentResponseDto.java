package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.PaymentMethod;
import cm.drivemaster.backend.enums.PaymentMotif;
import cm.drivemaster.backend.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PaymentResponseDto(
        @NotNull
        Long id,
        @NotNull
        DrivingSchoolResponseDto drivingSchool,
        @NotNull
        StudentsResponseDto student,
        @NotNull
        Double amount,
        @NotNull
        PaymentMethod method,
        @NotNull
        PaymentMotif motif,
        @NotNull
        PaymentStatus status,
        @NotNull
        LocalDate datePayment
) {
}
