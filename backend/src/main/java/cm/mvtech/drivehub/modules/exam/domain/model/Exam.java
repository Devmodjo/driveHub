package cm.mvtech.drivehub.modules.exam.domain.model;

import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

/** Session d'examen (code ou conduite) organisée par l'auto-école. */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exams")
public class Exam extends EntityBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driving_school_id")
    @JsonIgnore
    private DrivingSchool drivingSchool;

    @Column(name = "date_exams", nullable = false)
    private LocalDateTime dateExams;

    @Enumerated(EnumType.STRING)
    private LicenseCategory category;

    @OneToMany(mappedBy = "exams")
    @JsonIgnore
    private Set<ExamsInscription> examsInscriptionSet;
}
