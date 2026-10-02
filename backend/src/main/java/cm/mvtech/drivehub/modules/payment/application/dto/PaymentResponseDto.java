package cm.mvtech.drivehub.modules.payment.application.dto;

import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentMotif;
import cm.mvtech.drivehub.modules.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentResponseDto(
        UUID id,
        UUID studentId,
        String studentFirstname,
        String studentLastname,
        BigDecimal amount,
        PaymentMethod method,
        PaymentMotif motif,
        PaymentStatus paymentStatus,
        LocalDate datePayment
) {
}
