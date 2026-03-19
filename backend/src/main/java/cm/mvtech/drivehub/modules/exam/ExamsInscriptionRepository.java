package cm.mvtech.drivehub.modules.exam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamsInscriptionRepository extends JpaRepository<ExamsInscription, Long> {
}
