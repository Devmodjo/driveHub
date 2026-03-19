package cm.mvtech.drivehub.modules.course.infrastructure;

import cm.mvtech.drivehub.modules.course.domain.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesRepository extends JpaRepository<Course, Long> {
}
