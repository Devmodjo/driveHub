package cm.mvtech.drivehub.core.domain.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Liste des tenants (schémas) connus de la plateforme.
 * Consultée à chaque requête par {@code TenantService.isValidTenant}.
 */
@Entity
@Getter
@Setter
@Table(name = "tenants", schema = "public")
@NoArgsConstructor
public class TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom du schéma PostgreSQL (= DrivingSchoolRegistry.schemaName). */
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private boolean active;

    public TenantEntity(String code) {
        this.code = code;
    }
}
