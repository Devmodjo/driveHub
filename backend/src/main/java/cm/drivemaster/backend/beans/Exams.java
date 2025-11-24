package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.LicenseCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exams")
public class Exams {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driving_school_id")
    @JsonIgnore
    private DrivingSchool drivingSchool;

    @Column(nullable = false)
    private Date dateExams;

    @Enumerated(EnumType.STRING)
    private LicenseCategory category;
}
