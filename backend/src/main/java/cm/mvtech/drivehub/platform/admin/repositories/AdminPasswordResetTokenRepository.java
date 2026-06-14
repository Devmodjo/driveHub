package cm.mvtech.drivehub.platform.admin.repositories;

import cm.mvtech.drivehub.platform.admin.models.AdminPasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminPasswordResetTokenRepository
        extends JpaRepository<AdminPasswordResetToken, UUID> {

    Optional<AdminPasswordResetToken> findByToken(String token);
    void deleteAllByAdminId(UUID adminId);
}