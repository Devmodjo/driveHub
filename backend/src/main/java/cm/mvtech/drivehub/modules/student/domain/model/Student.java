package cm.mvtech.drivehub.modules.student.domain.model;


import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.reservation.domain.model.Reservation;
import cm.mvtech.drivehub.modules.enums.Gender;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.exam.domain.model.ExamsInscription;
import cm.mvtech.drivehub.modules.payment.domain.model.Payment;
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
@Table(name = "students") // pas de schéma fixe : suit le schéma du tenant courant
public class Student extends EntityBase {

    @ManyToOne(fetch = FetchType.LAZY)
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
    @JsonIgnore
    private Set<ExamsInscription> examsInscriptionSet;

    @OneToMany(mappedBy = "student")
    @JsonIgnore
    private Set<Reservation> reservations;

    @OneToMany(mappedBy = "student")
    @JsonIgnore
    private Set<Payment> payments;
}
