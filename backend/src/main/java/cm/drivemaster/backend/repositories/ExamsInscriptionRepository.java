package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.ExamsInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamsInscriptionRepository extends JpaRepository<ExamsInscription, Long> {
}
