package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.JoinStatus;
import cm.drivemaster.backend.enums.Role;
import jakarta.persistence.*;
import lombok.*;

/**
 * table que consulte l'admin afin de savoir quel utilisateur veut s'inscrire
 * dans son auto-ecole (schema public)
 */


@Entity
@Table(name = "school_join_request")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolJoinRequest extends EntityBase {

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private Long drivingSchoolId; // ID registry (public)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role requestedRole; // STUDENT ou MONITOR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinStatus joinStatus; // PENDING, APPROVED, REJECTED
}

