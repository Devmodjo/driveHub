package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.PaymentMethod;
import cm.drivemaster.backend.enums.PaymentMotif;
import cm.drivemaster.backend.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDto(
        @NotNull
        Long drivingSchoolId,
        @NotNull
        Long studentsId,
        @NotNull
        Double amount,
        @NotNull
        PaymentMethod method,
        @NotNull
        PaymentMotif motif,
        @NotNull
        PaymentStatus status
) {
}
