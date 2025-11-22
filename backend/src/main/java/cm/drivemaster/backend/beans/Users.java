package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.Role;
import cm.drivemaster.backend.enums.Statut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.Set;

/* Entité représentative d'un utilisateur dans notre system*/

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "_users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role roles;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Statut statut;

    /**
     * par defaut on considère que le profile n'est pas complet
     */
    @Column(nullable = false)
    private Boolean fullProfile = false;

    @CreationTimestamp
    private LocalDate createdAt;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private Set<DrivingSchool> drivingSchool;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private Set<Students> students;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private Set<Monitors> monitors;
}
