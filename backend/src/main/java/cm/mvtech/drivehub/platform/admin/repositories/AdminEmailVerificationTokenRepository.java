package cm.mvtech.drivehub.platform.admin.repositories;

import cm.mvtech.drivehub.platform.admin.models.AdminEmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminEmailVerificationTokenRepository
        extends JpaRepository<AdminEmailVerificationToken, UUID> {

    Optional<AdminEmailVerificationToken> findByToken(String token);
    void deleteAllByAdminId(UUID adminId);
}