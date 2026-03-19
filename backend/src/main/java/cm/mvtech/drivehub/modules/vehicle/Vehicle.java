package cm.mvtech.drivehub.modules.vehicle;


import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.enums.State;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
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
