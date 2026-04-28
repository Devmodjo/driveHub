
package cm.mvtech.drivehub.modules.auth.infrastructure.repository;

import cm.mvtech.drivehub.modules.auth.domain.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByToken(String token);

    // Invalider les anciens tokens si l'user en redemande un
    void deleteAllByUserId(UUID userId);
}