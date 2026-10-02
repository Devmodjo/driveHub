package cm.mvtech.drivehub.modules.course.domain.services;

import cm.mvtech.drivehub.modules.course.application.dto.CoursesRequestDto;
import cm.mvtech.drivehub.modules.course.application.dto.CoursesResponseDto;
import cm.mvtech.drivehub.modules.course.domain.model.Course;
import cm.mvtech.drivehub.modules.course.infrastructure.mapper.CoursesMapper;
import cm.mvtech.drivehub.modules.course.infrastructure.repository.CoursesRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Cours (code de la route, théorie) publiés par l'auto-école. */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CoursesRepository coursesRepository;
    private final CoursesMapper mapper;
    private final CurrentSchoolProvider currentSchoolProvider;

    @Transactional(readOnly = true)
    public ApiPageResponse<CoursesResponseDto> list(Pageable pageable) {
        return ApiPageResponse.from(coursesRepository.findAllBy(pageable).map(mapper::fromEntityToResponse));
    }

    @Transactional(readOnly = true)
    public CoursesResponseDto get(UUID id) {
        return mapper.fromEntityToResponse(getEntity(id));
    }

    @Transactional
    public CoursesResponseDto create(CoursesRequestDto request) {
        Course course = new Course();
        mapper.updateEntity(request, course);
        course.setDrivingSchool(currentSchoolProvider.get());
        return mapper.fromEntityToResponse(coursesRepository.save(course));
    }

    @Transactional
    public CoursesResponseDto update(UUID id, CoursesRequestDto request) {
        Course course = getEntity(id);
        mapper.updateEntity(request, course);
        return mapper.fromEntityToResponse(course);
    }

    @Transactional
    public void delete(UUID id) {
        getEntity(id).markDeleted();
    }

    private Course getEntity(UUID id) {
        return coursesRepository.findById(id)
                .filter(course -> !course.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cours introuvable : " + id));
    }
}
