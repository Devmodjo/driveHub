package cm.mvtech.drivehub.modules.payment;

import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentMotif;
import cm.mvtech.drivehub.modules.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentRequestDto(
        @NotNull
        UUID drivingSchoolId,
        @NotNull
        UUID studentsId,
        @NotNull
        Double amount,
        @NotNull
        PaymentMethod method,
        @NotNull
        PaymentMotif motif,
        @NotNull
        PaymentStatus paymentStatus
) {
}
