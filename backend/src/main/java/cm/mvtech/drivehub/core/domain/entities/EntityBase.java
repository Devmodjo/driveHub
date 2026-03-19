package cm.mvtech.drivehub.core.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
public class EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    protected Long id;

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime createdOn;

    @Column(name = "last_update_on")
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    protected LocalDateTime lastUpdateOn;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    protected boolean deleted;

    @Column(name = "deleted_at")
    @LastModifiedDate
    protected LocalDateTime deletedAt;

    @Basic(optional = false)
    @Column(name = "status", nullable = false)
    protected short status;

    public EntityBase() {
        super();
        this.createdOn = LocalDateTime.now();
    }

    public EntityBase(Long id) {
        this.id = id;
    }

    public EntityBase(Long id, LocalDateTime createdOn) {
        this.id = id;
        this.createdOn = createdOn;
    }

    public EntityBase(EntityBaseDTO entityBaseDTO) {
        this.id = entityBaseDTO.getId();
        this.createdOn = entityBaseDTO.getCreatedOn();
        this.lastUpdateOn = entityBaseDTO.getLastUpdateOn();
        this.status = entityBaseDTO.getStatus();
    }

    public static EntityBaseDTO fromEntityBase(EntityBase entity) {
        return new EntityBaseDTO(entity);
    }

}
