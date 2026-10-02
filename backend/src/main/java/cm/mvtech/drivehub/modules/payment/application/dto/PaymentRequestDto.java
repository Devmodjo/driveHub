package cm.mvtech.drivehub.modules.payment.application.dto;

import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentMotif;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Paiement d'une formation ou d'un examen.
 *
 * <ul>
 *   <li>Élève : paie en ligne par Mobile Money (MOMO / OM) → {@code phoneNumber} obligatoire,
 *       le paiement passe par Campay et reste PENDING jusqu'à validation sur son téléphone.</li>
 *   <li>Moniteur : enregistre un paiement reçu (espèces...) → {@code studentsId} obligatoire, statut VALIDATE.</li>
 * </ul>
 * Le statut n'est jamais envoyé par le client.
 */
public record PaymentRequestDto(
        UUID studentsId,
        @NotNull @DecimalMin(value = "1", message = "le montant doit être positif")
        @Digits(integer = 10, fraction = 0, message = "le franc CFA n'a pas de centimes") BigDecimal amount,
        @NotNull PaymentMethod method,
        @NotNull PaymentMotif motif,
        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "numéro de téléphone invalide") String phoneNumber
) {
}
