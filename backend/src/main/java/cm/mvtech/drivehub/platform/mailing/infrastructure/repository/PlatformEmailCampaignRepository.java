package cm.mvtech.drivehub.platform.mailing.infrastructure.repository;

import cm.mvtech.drivehub.platform.mailing.domain.model.PlatformEmailCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlatformEmailCampaignRepository extends JpaRepository<PlatformEmailCampaign, UUID> {

    /** Historique, du plus récent au plus ancien. */
    Page<PlatformEmailCampaign> findAllByOrderBySentAtDesc(Pageable pageable);
}
