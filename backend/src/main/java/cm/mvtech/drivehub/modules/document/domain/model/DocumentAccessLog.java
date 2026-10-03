package cm.mvtech.drivehub.modules.document.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** Une consultation d'un justificatif (table public.document_access_logs). Jamais modifiée ni supprimée. */
@Entity
@Table(name = "document_access_logs", schema = "public")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    /** Email de la personne qui a consulté. */
    @Column(name = "accessed_by", nullable = false, length = 150)
    private String accessedBy;

    /** OWNER (le titulaire), SCHOOL_OWNER (responsable d'auto-école), PLATFORM_ADMIN. */
    @Column(name = "actor_role", nullable = false, length = 30)
    private String actorRole;

    /** Écran ou parcours d'origine (ex : JOIN_REQUEST, SCHOOL_REVIEW, STUDENT_FILE, SELF). */
    @Column(nullable = false, length = 50)
    private String context;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;
}
