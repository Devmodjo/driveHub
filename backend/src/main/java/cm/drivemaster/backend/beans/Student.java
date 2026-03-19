package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.Gender;
import cm.drivemaster.backend.enums.LicenseCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;


@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "students")
public class Student extends EntityBase{

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Date dateOfBirth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String nationality;

    @Column(nullable = false)
    private String residenceCity;

    @Column(nullable = true)
    private String cniRectoUrl;

    @Column(nullable = true)
    private String cniVersoUrl;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private LicenseCategory licenseCategory;

    @OneToMany(mappedBy = "student")
    private Set<ExamsInscription> examsInscriptionSet;

    @OneToMany(mappedBy = "student")
    private Set<Reservation> reservations;

    @OneToMany(mappedBy = "student")
    private Set<Payment> payments;
}
