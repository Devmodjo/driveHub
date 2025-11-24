package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.ReservationStatus;
import cm.drivemaster.backend.enums.ReservationTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reservations")
public class Reservations {

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

    @ManyToOne
    @JoinColumn(name = "monitor_id")
    @JsonIgnore
    private Monitors monitors;

    @CreationTimestamp
    private LocalDateTime dateTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationTypes types;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

}
