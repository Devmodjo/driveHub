package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.InscriptionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;


@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ExamsInscription extends EntityBase{

    @ManyToOne
    @JoinColumn(name = "exams_id")
    @JsonIgnore
    private Exam exams;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;

    @Enumerated(EnumType.STRING)
    private InscriptionStatus inscriptionStatus;

    @CreationTimestamp
    private LocalDate registeredAt;
}
