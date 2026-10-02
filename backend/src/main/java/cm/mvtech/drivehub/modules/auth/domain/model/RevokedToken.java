package cm.mvtech.drivehub.modules.auth.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Jeton JWT invalidé par une déconnexion (table public.revoked_tokens, migration V10).
 *
 * <p>On ne garde que l'empreinte SHA-256 du jeton ({@code tokenHash}) et sa date d'expiration :
 * après cette date le jeton est refusé de toute façon, la ligne peut donc être supprimée.</p>
 */
@Entity
@Table(name = "revoked_tokens", schema = "public")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;
}
