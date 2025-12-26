package cm.drivemaster.backend.beans;

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

    @OneToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @CreationTimestamp
    private LocalDate createdAt;
}
