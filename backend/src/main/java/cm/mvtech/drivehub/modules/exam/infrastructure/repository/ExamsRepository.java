package cm.mvtech.drivehub.modules.exam.infrastructure.repository;

import cm.mvtech.drivehub.modules.exam.domain.model.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Correction : identifiant UUID (et non Long). */
@Repository
public interface ExamsRepository extends JpaRepository<Exam, UUID> {

    Page<Exam> findAllBy(Pageable pageable);
}
