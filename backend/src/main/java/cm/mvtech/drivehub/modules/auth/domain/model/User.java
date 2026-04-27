package cm.mvtech.drivehub.modules.auth.domain.model;


import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.student.Student;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.Set;

/* Entité représentative d'un utilisateur dans notre system*/

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "_users", schema = "public")
public class User extends EntityBase {

    @Column(nullable = false)
    private String firstname;

    private String lastname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role roles;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProfileStatus profileStatus;

    /**
     * Par defaut, on considère que le profile n'est pas complet
     */
    @Column(nullable = false)
    private Boolean fullProfile = false;

    @CreationTimestamp
    private LocalDate createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<DrivingSchool> drivingSchool;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Student> students;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Monitor> monitors;
}
