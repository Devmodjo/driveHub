package cm.mvtech.drivehub.platform.admin.models;


import cm.mvtech.drivehub.core.domain.entities.EntityBase;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "plateform_admin")
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAdmin extends EntityBase {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;


    @Enumerated(value = EnumType.STRING)
    private AdminRole role;

    @Enumerated(value = EnumType.STRING)
    private AdminStatus adminStatus;

    @Column(nullable = false)
    private String password;

    private boolean enabled;

    @Column(nullable = false)
    private String residence; // ville, lieu
    private String phoneNumber;


    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;


}
