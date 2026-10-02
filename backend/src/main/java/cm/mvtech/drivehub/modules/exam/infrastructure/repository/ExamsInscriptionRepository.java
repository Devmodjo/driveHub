package cm.mvtech.drivehub.modules.exam.infrastructure.repository;

import cm.mvtech.drivehub.modules.exam.domain.model.ExamsInscription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamsInscriptionRepository extends JpaRepository<ExamsInscription, UUID> {

    boolean existsByExams_IdAndStudent_Id(UUID examId, UUID studentId);

    @EntityGraph(attributePaths = {"exams", "student", "student.user"})
    List<ExamsInscription> findAllByExams_IdOrderByRegisteredAtAsc(UUID examId);

    @EntityGraph(attributePaths = {"exams", "student", "student.user"})
    List<ExamsInscription> findAllByStudent_IdOrderByRegisteredAtDesc(UUID studentId);
}
