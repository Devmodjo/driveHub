package cm.mvtech.drivehub.modules.student.infrastructure.repository;

import cm.mvtech.drivehub.modules.student.domain.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Élèves du schéma courant (public à l'inscription, tenant une fois rattachés à une auto-école). */
@Repository
public interface StudentsRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findFirstByUser_Id(UUID userId);

    /** @EntityGraph : charge le compte utilisateur dans la même requête (évite N+1). */
    @EntityGraph(attributePaths = "user")
    Page<Student> findAllBy(Pageable pageable);
}
