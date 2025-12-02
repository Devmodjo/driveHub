package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.beans.EntityBase;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EntityBaseDTO {
    protected Long id;
    protected LocalDateTime createdOn;
    protected LocalDateTime lastUpdateOn;
    protected short status;

    public EntityBaseDTO() {
        super();
        this.createdOn = LocalDateTime.now();
    }

    public EntityBaseDTO(EntityBase entityBase) {
        this.id = entityBase.getId();
        this.createdOn = entityBase.getCreatedOn();
        this.lastUpdateOn = entityBase.getLastUpdateOn();
    }

    public EntityBaseDTO(Long id, LocalDateTime lastUpdateOn) {
        this.id = id;
        this.lastUpdateOn = lastUpdateOn;
    }

    public EntityBaseDTO(Long id) {
        this.id = id;
    }

}
