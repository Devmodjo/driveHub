package cm.mvtech.drivehub.modules.payment.application.dto;

import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentMotif;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Enregistrement d'un paiement.
 *
 * <p>Le statut n'est plus envoyé par le client (il pouvait se déclarer "VALIDATE" lui-même) :
 * il est fixé par le service selon qui paie (élève -> PENDING, moniteur -> VALIDATE).</p>
 *
 * @param studentsId obligatoire si c'est le moniteur qui enregistre ; ignoré pour l'élève
 */
public record PaymentRequestDto(
        UUID studentsId,
        @NotNull @DecimalMin(value = "1", message = "le montant doit être positif")
        @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull PaymentMethod method,
        @NotNull PaymentMotif motif
) {
}
