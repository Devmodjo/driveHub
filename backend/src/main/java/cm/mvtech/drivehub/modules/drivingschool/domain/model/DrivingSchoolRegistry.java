package cm.mvtech.drivehub.modules.drivingschool.domain.model;

import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
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
public class DrivingSchoolRegistry extends EntityBase {

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
