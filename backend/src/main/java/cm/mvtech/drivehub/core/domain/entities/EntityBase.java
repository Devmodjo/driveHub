package cm.mvtech.drivehub.core.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe mère de toutes les entités.
 *
 * <p>Corrections apportées :</p>
 * <ul>
 *   <li>{@code @Getter/@Setter} au lieu de {@code @Data} : {@code @Data} génère equals/hashCode/toString
 *       sur TOUS les champs, ce qui casse les collections JPA ({@code Set}) quand l'id change à la
 *       sauvegarde et peut provoquer des boucles infinies (toString sur des relations bidirectionnelles).</li>
 *   <li>{@code @CreationTimestamp/@UpdateTimestamp} (Hibernate) au lieu de {@code @LastModifiedDate} :
 *       ce dernier n'est rempli que si {@code @EnableJpaAuditing} est activé, ce qui n'était pas le cas
 *       (last_update_on restait toujours NULL).</li>
 *   <li>{@code deletedAt} n'est plus annoté {@code @LastModifiedDate} : il était mis à jour à chaque
 *       modification, même sans suppression.</li>
 *   <li>{@code @SQLRestriction} remplace {@code @Where}, déprécié depuis Hibernate 6.3.</li>
 * </ul>
 */
@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    protected UUID id;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    protected LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "last_update_on")
    protected LocalDateTime lastUpdateOn;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    protected boolean deleted;

    /** Renseigné uniquement lors d'une suppression logique (voir {@link #markDeleted()}). */
    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Basic(optional = false)
    @Column(name = "status", nullable = false)
    protected short status;

    public EntityBase() {
        super();
    }

    public EntityBase(UUID id) {
        this.id = id;
    }

    public EntityBase(UUID id, LocalDateTime createdOn) {
        this.id = id;
        this.createdOn = createdOn;
    }

    public EntityBase(EntityBaseDTO entityBaseDTO) {
        this.id = entityBaseDTO.getId();
        this.createdOn = entityBaseDTO.getCreatedOn();
        this.lastUpdateOn = entityBaseDTO.getLastUpdateOn();
        this.status = entityBaseDTO.getStatus();
    }

    /** Suppression logique : la ligne reste en base mais n'apparaît plus dans les requêtes. */
    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public static EntityBaseDTO fromEntityBase(EntityBase entity) {
        return new EntityBaseDTO(entity);
    }

}
