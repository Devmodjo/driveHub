package cm.drivemaster.backend.beans;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.Set;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "driving_school")
public class DrivingSchool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Users users;

    @CreationTimestamp
    private LocalDate createdAt;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Monitors> monitors;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Vehicles> vehicles;

    @OneToMany(mappedBy = "drivingSchool")
    private Set<Courses> courses;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Exams> exams;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Reservations> reservations;

    @OneToMany(mappedBy = "drivingSchool", cascade = CascadeType.ALL)
    private Set<Payments> payments;
}
