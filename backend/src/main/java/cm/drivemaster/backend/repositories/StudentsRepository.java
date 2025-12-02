package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentsRepository extends JpaRepository<Student, Long> {
}
