package cm.mvtech.drivehub.modules.drivingschool.domain.model;


import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * table que consulte l'admin afin de savoir quel utilisateur veut s'inscrire
 * dans son auto-ecole (schema public) ancien workflow systeme
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
    private UUID drivingSchoolId; // ID registry (public)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role requestedRole; // STUDENT ou MONITOR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinStatus joinStatus; // PENDING, APPROVED, REJECTED
}

