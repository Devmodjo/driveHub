package cm.drivemaster.backend.beans;


import cm.drivemaster.backend.enums.AdminRole;
import cm.drivemaster.backend.enums.AdminStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
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
    private AdminStatus status;

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
