package cm.mvtech.drivehub.modules.payment.domain.model;

import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentMotif;
import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.enums.PaymentStatus;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Paiement d'un élève (inscription ou examen), stocké dans le schéma de l'auto-école.
 * Montant en BigDecimal / numeric(12,2) : jamais de Double pour de l'argent.
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments")
public class Payment extends EntityBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driving_school_id")
    @JsonIgnore
    private DrivingSchool drivingSchool;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMotif motif;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDate datePayment;

    /** MANUAL (saisi par le moniteur), SIMULATED ou CAMPAY. */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider = PaymentProvider.MANUAL;

    /** Numéro Mobile Money débité (paiements en ligne). */
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    /** Référence de la transaction chez l'agrégateur. */
    @Column(name = "external_reference", length = 100)
    private String externalReference;

    /** Information à afficher à l'élève (code USSD, motif de refus...). */
    @Column(name = "gateway_message")
    private String gatewayMessage;

    /**
     * Date du paiement fixée juste avant l'insertion.
     * (@CreationTimestamp laissait la colonne vide quand le paiement était modifié entre save() et
     * l'écriture en base, ce qui arrive avec la réponse de la passerelle Mobile Money.)
     */
    @PrePersist
    void initDatePayment() {
        if (datePayment == null) {
            datePayment = LocalDate.now();
        }
    }
}
