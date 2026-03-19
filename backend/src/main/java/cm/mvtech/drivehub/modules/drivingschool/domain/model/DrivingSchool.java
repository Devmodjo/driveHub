package cm.mvtech.drivehub.modules.drivingschool.domain.model;



import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.reservation.Reservation;
import cm.mvtech.drivehub.modules.vehicle.Vehicle;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.course.domain.model.Course;
import cm.mvtech.drivehub.modules.exam.Exam;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.payment.Payment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.Set;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "driving_school")
public class DrivingSchool extends EntityBase {

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    private String country;
    private String city;
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;
    private String description;
    private String websiteUrl;
    private String whatsappNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DrivingSchoolStatus drivingSchoolStatus;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @CreationTimestamp
    private LocalDate createdAt;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Monitor> monitors;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Vehicle> vehicles;

    @OneToMany(mappedBy = "drivingSchool")
    private Set<Course> courses;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Exam> exams;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Reservation> reservations;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Payment> payments;
}
