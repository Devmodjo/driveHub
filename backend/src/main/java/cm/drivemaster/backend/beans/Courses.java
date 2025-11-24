package cm.drivemaster.backend.beans;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
public class Courses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driving_school_id")
    @JsonIgnore
    private DrivingSchool drivingSchool;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    private LocalDate createdAt;

}
