package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.LicenseCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exams")
public class Exam extends EntityBase {

    @ManyToOne
    @JoinColumn(name = "driving_school_id")
    @JsonIgnore
    private DrivingSchool drivingSchool;

    @Column(nullable = false)
    private Date dateExams;

    @Enumerated(EnumType.STRING)
    private LicenseCategory category;

    @OneToMany(mappedBy = "exams", cascade = CascadeType.ALL)
    private Set<ExamsInscription> examsInscriptionSet;
}
