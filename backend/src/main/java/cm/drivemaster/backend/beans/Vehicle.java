package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.State;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


/**
 * Classe representative des vehicules dans notre system
 */

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vehicles")
public class Vehicle extends EntityBase {

    @ManyToOne
    @JoinColumn(name = "driving_school_id")
    @JsonIgnore
    private DrivingSchool drivingSchool;

    @Column(nullable = false)
    private String matriculation;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

}
