package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamsRepository extends JpaRepository<Exam, Long> {
}
