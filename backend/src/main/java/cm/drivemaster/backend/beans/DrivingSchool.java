package cm.drivemaster.backend.beans;


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
public class DrivingSchool extends EntityBase{

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;
    private String description;

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
