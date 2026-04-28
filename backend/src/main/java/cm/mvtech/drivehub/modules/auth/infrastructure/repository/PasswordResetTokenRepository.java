// PasswordResetTokenRepository.java
package cm.mvtech.drivehub.modules.auth.infrastructure.repository;

import cm.mvtech.drivehub.modules.auth.domain.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteAllByUserId(UUID userId);
}