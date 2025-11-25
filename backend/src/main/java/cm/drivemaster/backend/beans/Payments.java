package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.PaymentMethod;
import cm.drivemaster.backend.enums.PaymentMotif;
import cm.drivemaster.backend.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments")
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driving_school_id")
    @JsonIgnore
    private DrivingSchool drivingSchool;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Students students;

    @Positive
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMotif motif;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDate datePayment;

}
