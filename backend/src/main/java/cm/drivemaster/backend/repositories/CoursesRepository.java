package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesRepository extends JpaRepository<Course, Long> {
}
