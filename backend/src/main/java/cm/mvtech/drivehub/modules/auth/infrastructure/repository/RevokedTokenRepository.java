package cm.mvtech.drivehub.modules.auth.infrastructure.repository;

import cm.mvtech.drivehub.modules.auth.domain.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    /** Supprime les jetons déjà expirés (ils seraient refusés de toute façon). */
    @Modifying
    @Query("DELETE FROM RevokedToken t WHERE t.expiresAt < :now")
    int deleteExpired(LocalDateTime now);
}
