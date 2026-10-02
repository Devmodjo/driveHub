package cm.mvtech.drivehub.modules.course.infrastructure.repository;

import cm.mvtech.drivehub.modules.course.domain.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoursesRepository extends JpaRepository<Course, UUID> {

    Page<Course> findAllBy(Pageable pageable);
}
