package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.InscriptionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ExamsInscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exams_id")
    @JsonIgnore
    private Exams exams;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Students students;

    @Enumerated(EnumType.STRING)
    private InscriptionStatus status;

    @CreationTimestamp
    private LocalDate registeredAt;
}
