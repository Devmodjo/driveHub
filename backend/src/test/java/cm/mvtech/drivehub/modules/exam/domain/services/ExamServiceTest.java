package cm.mvtech.drivehub.modules.exam.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.InscriptionStatus;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsInscriptionResponseDto;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsRequestDto;
import cm.mvtech.drivehub.modules.exam.application.dto.ExamsResponseDto;
import cm.mvtech.drivehub.modules.exam.domain.model.Exam;
import cm.mvtech.drivehub.modules.exam.domain.model.ExamsInscription;
import cm.mvtech.drivehub.modules.exam.infrastructure.mapper.ExamsInscriptionMapper;
import cm.mvtech.drivehub.modules.exam.infrastructure.mapper.ExamsMapper;
import cm.mvtech.drivehub.modules.exam.infrastructure.repository.ExamsInscriptionRepository;
import cm.mvtech.drivehub.modules.exam.infrastructure.repository.ExamsRepository;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.domain.services.StudentService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link ExamService} : sessions d'examen et inscriptions des élèves.
 *
 * <p>Règles d'inscription vérifiées : pas de double inscription au même examen, et la catégorie
 * de permis préparée par l'élève doit être celle de l'examen.</p>
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock private ExamsRepository examsRepository;
    @Mock private ExamsInscriptionRepository inscriptionRepository;
    @Mock private StudentService studentService;
    @Mock private CurrentSchoolProvider currentSchoolProvider;
    @Spy  private ExamsMapper examsMapper = Mappers.getMapper(ExamsMapper.class);
    @Spy  private ExamsInscriptionMapper inscriptionMapper = Mappers.getMapper(ExamsInscriptionMapper.class);

    @InjectMocks
    private ExamService service;

    private DrivingSchool school;
    private Exam exam;
    private Student student;

    @BeforeEach
    void setUp() {
        school = TestData.school();
        exam = new Exam();
        exam.setId(UUID.randomUUID());
        exam.setDateExams(LocalDateTime.now().plusMonths(2));
        exam.setCategory(LicenseCategory.B);
        exam.setDrivingSchool(school);

        User studentUser = TestData.user(Role.STUDENT);
        student = TestData.student(studentUser);   // permis B par défaut
    }

    // ─── sessions ─────────────────────────────────────────────────────────────

    @Test
    void create_ShouldAttachSchoolAndSave() {
        when(currentSchoolProvider.get()).thenReturn(school);
        when(examsRepository.save(any(Exam.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime date = LocalDateTime.now().plusDays(30);

        ExamsResponseDto dto = service.create(new ExamsRequestDto(date, LicenseCategory.A));

        ArgumentCaptor<Exam> captor = ArgumentCaptor.forClass(Exam.class);
        verify(examsRepository).save(captor.capture());
        assertSame(school, captor.getValue().getDrivingSchool());
        assertEquals(date, dto.dateExams());
        assertEquals(LicenseCategory.A, dto.category());
    }

    @Test
    void update_UnknownExam_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(examsRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(unknown, new ExamsRequestDto(LocalDateTime.now().plusDays(1), LicenseCategory.B)));
    }

    @Test
    void delete_ShouldMarkExamAsDeleted() {
        when(examsRepository.findById(exam.getId())).thenReturn(Optional.of(exam));

        service.delete(exam.getId());

        assertTrue(exam.isDeleted());
    }

    // ─── inscriptions ─────────────────────────────────────────────────────────

    /** Cas nominal : même catégorie de permis, pas encore inscrit → inscription au statut INSCRIT. */
    @Test
    void register_SameCategory_ShouldCreateInscription() {
        when(examsRepository.findById(exam.getId())).thenReturn(Optional.of(exam));
        when(studentService.getEntity(student.getId())).thenReturn(student);
        when(inscriptionRepository.existsByExams_IdAndStudent_Id(exam.getId(), student.getId())).thenReturn(false);
        when(inscriptionRepository.save(any(ExamsInscription.class))).thenAnswer(inv -> inv.getArgument(0));

        ExamsInscriptionResponseDto dto = service.register(exam.getId(), student.getId());

        assertEquals(exam.getId(), dto.examId());
        assertEquals(student.getId(), dto.studentId());
        assertEquals(InscriptionStatus.INSCRIT, dto.inscriptionStatus());
        assertEquals(LicenseCategory.B, dto.category());
    }

    /** Double inscription au même examen : 409. */
    @Test
    void register_AlreadyRegistered_ShouldThrowConflict() {
        when(examsRepository.findById(exam.getId())).thenReturn(Optional.of(exam));
        when(studentService.getEntity(student.getId())).thenReturn(student);
        when(inscriptionRepository.existsByExams_IdAndStudent_Id(exam.getId(), student.getId())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.register(exam.getId(), student.getId()));
        verify(inscriptionRepository, never()).save(any());
    }

    /** L'élève prépare le permis A mais l'examen concerne le permis B : 400. */
    @Test
    void register_DifferentLicenseCategory_ShouldThrowBadRequest() {
        student.setLicenseCategory(LicenseCategory.A);
        when(examsRepository.findById(exam.getId())).thenReturn(Optional.of(exam));
        when(studentService.getEntity(student.getId())).thenReturn(student);

        assertThrows(BadRequestException.class, () -> service.register(exam.getId(), student.getId()));
        verify(inscriptionRepository, never()).save(any());
    }

    /** Non-régression : un élève sans catégorie de permis pouvait être inscrit à n'importe quel examen. */
    @Test
    void register_StudentWithoutLicenseCategory_ShouldThrowBadRequest() {
        student.setLicenseCategory(null);
        when(examsRepository.findById(exam.getId())).thenReturn(Optional.of(exam));
        when(studentService.getEntity(student.getId())).thenReturn(student);

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> service.register(exam.getId(), student.getId()));
        assertTrue(error.getMessage().startsWith("Définissez d'abord la catégorie de permis"));
        verify(inscriptionRepository, never()).save(any());
    }

    @Test
    void register_UnknownExam_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(examsRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.register(unknown, student.getId()));
        verifyNoInteractions(studentService);
    }

    @Test
    void updateInscriptionStatus_ShouldChangeStatus() {
        ExamsInscription inscription = new ExamsInscription(exam, student, InscriptionStatus.INSCRIT, null);
        inscription.setId(UUID.randomUUID());
        when(inscriptionRepository.findById(inscription.getId())).thenReturn(Optional.of(inscription));

        ExamsInscriptionResponseDto dto = service.updateInscriptionStatus(inscription.getId(), InscriptionStatus.REFUSE);

        assertEquals(InscriptionStatus.REFUSE, inscription.getInscriptionStatus());
        assertEquals(InscriptionStatus.REFUSE, dto.inscriptionStatus());
    }

    @Test
    void updateInscriptionStatus_UnknownInscription_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(inscriptionRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateInscriptionStatus(unknown, InscriptionStatus.INSCRIT));
    }

    @Test
    void inscriptionsOfExam_ShouldListInscriptions() {
        ExamsInscription inscription = new ExamsInscription(exam, student, InscriptionStatus.INSCRIT, null);
        when(examsRepository.findById(exam.getId())).thenReturn(Optional.of(exam));
        when(inscriptionRepository.findAllByExams_IdOrderByRegisteredAtAsc(exam.getId())).thenReturn(List.of(inscription));

        List<ExamsInscriptionResponseDto> result = service.inscriptionsOfExam(exam.getId());

        assertEquals(1, result.size());
        assertEquals(student.getUser().getFirstname(), result.get(0).studentFirstname());
    }

    /** L'élève connecté ne voit que ses propres inscriptions. */
    @Test
    void myInscriptions_ShouldUseConnectedStudent() {
        ExamsInscription inscription = new ExamsInscription(exam, student, InscriptionStatus.INSCRIT, null);
        when(studentService.currentStudent()).thenReturn(student);
        when(inscriptionRepository.findAllByStudent_IdOrderByRegisteredAtDesc(student.getId())).thenReturn(List.of(inscription));

        List<ExamsInscriptionResponseDto> result = service.myInscriptions();

        assertEquals(1, result.size());
        assertEquals(student.getId(), result.get(0).studentId());
    }
}
