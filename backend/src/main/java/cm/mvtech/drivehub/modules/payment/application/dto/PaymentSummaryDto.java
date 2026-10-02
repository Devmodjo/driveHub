package cm.mvtech.drivehub.modules.payment.application.dto;

import java.math.BigDecimal;

/** Totaux des paiements de l'auto-école (tableau de bord du moniteur). */
public record PaymentSummaryDto(BigDecimal totalValidated, BigDecimal totalPending) {
}
