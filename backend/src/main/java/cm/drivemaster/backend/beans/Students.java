package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.LicenseCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "students")
public class Students {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(nullable = false)
    private String cniRectoUrl;

    @Column(nullable = false)
    private String cniVersoUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LicenseCategory licenseCategory;

    @OneToMany(mappedBy = "students")
    private Set<ExamsInscription> examsInscriptionSet;

    @OneToMany(mappedBy = "students")
    private Set<Reservations> reservations;
}
