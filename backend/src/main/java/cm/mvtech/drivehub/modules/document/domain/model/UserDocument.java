package cm.mvtech.drivehub.modules.document.domain.model;

import cm.mvtech.drivehub.core.security.crypto.EncryptedStringConverter;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Justificatif d'un utilisateur (table public.user_documents, migration V14).
 *
 * <p>Le fichier n'est pas ici : il est chiffré puis rangé dans le stockage sous {@link #objectKey}.
 * Les champs sensibles (nom du fichier, numéro du document) sont chiffrés automatiquement en base
 * par {@link EncryptedStringConverter} : le code Java les lit en clair.</p>
 */
@Entity
@Table(name = "user_documents", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    /** Emplacement dans le stockage (aléatoire, sans information personnelle). Sert aussi de donnée liée au chiffrement. */
    @Column(name = "object_key", nullable = false, unique = true)
    private String objectKey;

    /** Clé de chiffrement du fichier, chiffrée par la clé maître. */
    @Column(name = "wrapped_data_key", nullable = false, columnDefinition = "text")
    private String wrappedDataKey;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** SHA-256 du fichier en clair : vérifié après déchiffrement (intégrité). */
    @Column(nullable = false, length = 64)
    private String sha256;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "file_name_enc", columnDefinition = "text")
    private String fileName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "document_number_enc", columnDefinition = "text")
    private String documentNumber;

    /** Empreinte HMAC du numéro (recherche de doublons sans déchiffrer). */
    @Column(name = "document_number_hash", length = 64)
    private String documentNumberHash;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by", length = 150)
    private String reviewedBy;

    @Column(name = "review_comment", columnDefinition = "text")
    private String reviewComment;
}
