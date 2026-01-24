package cm.drivemaster.backend.beans;

import cm.drivemaster.backend.enums.DrivingSchoolStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

/**
 * login tenant-aware
 *
 * résolution du schéma
 *
 * isolation multi-tenant propre
 */
@Entity
@Table(name = "driving_school_registry", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrivingSchoolRegistry {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String schoolName;

    @Column(nullable = false, unique = true)
    private String schemaName;

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

    @OneToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @CreationTimestamp
    private LocalDate createdAt;
}
