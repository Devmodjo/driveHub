package cm.mvtech.drivehub.platform.mailing.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Un envoi d'email depuis le back-office (table public.platform_email_campaigns, migration V13). */
@Entity
@Table(name = "platform_email_campaigns", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class PlatformEmailCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailAudience audience;

    /** Auto-école visée (audience SCHOOL_MEMBERS uniquement). */
    private UUID schoolId;

    @Column(length = 150)
    private String schoolName;

    @Column(nullable = false)
    private int recipientCount;

    @Column(nullable = false, length = 150)
    private String sentByEmail;

    @Column(nullable = false)
    private LocalDateTime sentAt;
}
