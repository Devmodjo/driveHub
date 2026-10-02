package cm.mvtech.drivehub.modules.course.domain.services;

import cm.mvtech.drivehub.modules.course.application.dto.CoursesRequestDto;
import cm.mvtech.drivehub.modules.course.application.dto.CoursesResponseDto;
import cm.mvtech.drivehub.modules.course.domain.model.Course;
import cm.mvtech.drivehub.modules.course.infrastructure.mapper.CoursesMapper;
import cm.mvtech.drivehub.modules.course.infrastructure.repository.CoursesRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link CourseService} (cours publiés par l'auto-école). */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CoursesRepository coursesRepository;
    @Mock private CurrentSchoolProvider currentSchoolProvider;
    @Spy  private CoursesMapper mapper = Mappers.getMapper(CoursesMapper.class);

    @InjectMocks
    private CourseService service;

    private DrivingSchool school;
    private Course course;

    @BeforeEach
    void setUp() {
        school = TestData.school();
        course = new Course();
        course.setId(UUID.randomUUID());
        course.setTitle("Priorités");
        course.setContent("Priorité à droite");
        course.setDrivingSchool(school);
    }

    /** Cas nominal : le cours est rattaché à l'auto-école du tenant puis sauvegardé. */
    @Test
    void create_ShouldAttachSchoolAndSave() {
        when(currentSchoolProvider.get()).thenReturn(school);
        when(coursesRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CoursesResponseDto dto = service.create(new CoursesRequestDto("Signalisation", "Les panneaux de danger"));

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(coursesRepository).save(captor.capture());
        assertSame(school, captor.getValue().getDrivingSchool());
        assertEquals("Signalisation", dto.title());
        assertEquals("Les panneaux de danger", dto.content());
    }

    @Test
    void get_ExistingCourse_ShouldReturnDto() {
        when(coursesRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertEquals("Priorités", service.get(course.getId()).title());
    }

    @Test
    void get_UnknownOrDeletedCourse_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(coursesRepository.findById(unknown)).thenReturn(Optional.empty());
        course.markDeleted();
        when(coursesRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ResourceNotFoundException.class, () -> service.get(unknown));
        assertThrows(ResourceNotFoundException.class, () -> service.get(course.getId()));
    }

    @Test
    void update_ShouldReplaceTitleAndContent() {
        when(coursesRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CoursesResponseDto dto = service.update(course.getId(), new CoursesRequestDto("Nouveau titre", "Nouveau contenu"));

        assertEquals("Nouveau titre", course.getTitle());
        assertEquals("Nouveau contenu", dto.content());
        // L'auto-école n'est pas modifiée par une mise à jour
        assertSame(school, course.getDrivingSchool());
    }

    @Test
    void update_UnknownCourse_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(coursesRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(unknown, new CoursesRequestDto("T", "C")));
    }

    @Test
    void list_ShouldReturnMappedPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(coursesRepository.findAllBy(pageable)).thenReturn(new PageImpl<>(List.of(course), pageable, 1));

        ApiPageResponse<CoursesResponseDto> page = service.list(pageable);

        assertEquals(1, page.totalElements());
        assertEquals(course.getId(), page.content().get(0).id());
    }

    @Test
    void delete_ShouldMarkCourseAsDeleted() {
        when(coursesRepository.findById(course.getId())).thenReturn(Optional.of(course));

        service.delete(course.getId());

        assertTrue(course.isDeleted());
    }
}
