package cm.mvtech.drivehub.modules.student.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.student.application.dto.StudentsRequestDto;
import cm.mvtech.drivehub.modules.student.application.dto.StudentsResponseDto;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.infrastructure.mapper.StudentsMapper;
import cm.mvtech.drivehub.modules.student.infrastructure.repository.StudentsRepository;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link StudentService}.
 *
 * <p>Aucun contexte Spring n'est démarré : le repository et le fournisseur d'utilisateur
 * courant sont des « mocks » Mockito (de faux objets dont on programme les réponses).
 * Le mapper MapStruct, lui, est le VRAI mapper généré à la compilation ({@code @Spy}) :
 * on peut ainsi vérifier directement le contenu des DTO renvoyés.</p>
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentsRepository studentsRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Spy  private StudentsMapper mapper = Mappers.getMapper(StudentsMapper.class);

    @InjectMocks
    private StudentService service;

    private User studentUser;
    private Student student;

    @BeforeEach
    void setUp() {
        studentUser = TestData.user(Role.STUDENT);
        student = TestData.student(studentUser);
    }

    // ─── list ─────────────────────────────────────────────────────────────────

    /** La liste paginée est transformée en DTO, avec les informations du compte utilisateur. */
    @Test
    void list_ShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(studentsRepository.findAllBy(pageable)).thenReturn(new PageImpl<>(List.of(student), pageable, 1));

        ApiPageResponse<StudentsResponseDto> page = service.list(pageable);

        assertEquals(1, page.totalElements());
        assertEquals(student.getId(), page.content().get(0).id());
        assertEquals(studentUser.getEmail(), page.content().get(0).email());
    }

    // ─── get / getEntity ──────────────────────────────────────────────────────

    @Test
    void get_ExistingStudent_ShouldReturnDto() {
        when(studentsRepository.findById(student.getId())).thenReturn(Optional.of(student));

        StudentsResponseDto dto = service.get(student.getId());

        assertEquals(student.getId(), dto.id());
        assertEquals(studentUser.getId(), dto.userId());
        assertEquals(LicenseCategory.B, dto.licenseCategory());
    }

    @Test
    void get_UnknownStudent_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(studentsRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.get(unknown));
    }

    /** Un élève supprimé logiquement (deleted = true) doit être considéré comme introuvable. */
    @Test
    void getEntity_DeletedStudent_ShouldThrowResourceNotFound() {
        student.markDeleted();
        when(studentsRepository.findById(student.getId())).thenReturn(Optional.of(student));

        assertThrows(ResourceNotFoundException.class, () -> service.getEntity(student.getId()));
    }

    // ─── me / currentStudent ──────────────────────────────────────────────────

    /** L'élève connecté retrouve SA fiche grâce à l'identifiant de son compte. */
    @Test
    void me_ShouldReturnStudentOfConnectedUser() {
        when(currentUserProvider.get()).thenReturn(TestData.principal(studentUser));
        when(studentsRepository.findFirstByUser_Id(studentUser.getId())).thenReturn(Optional.of(student));

        StudentsResponseDto dto = service.me();

        assertEquals(student.getId(), dto.id());
    }

    /** Compte connecté mais aucune fiche élève dans ce tenant : 404. */
    @Test
    void currentStudent_WithoutStudentProfile_ShouldThrowResourceNotFound() {
        when(currentUserProvider.get()).thenReturn(TestData.principal(studentUser));
        when(studentsRepository.findFirstByUser_Id(studentUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.currentStudent());
    }

    // ─── update ───────────────────────────────────────────────────────────────

    /** Le moniteur complète le dossier : catégorie de permis et pièces d'identité. */
    @Test
    void update_ShouldCopyDossierFields() {
        when(studentsRepository.findById(student.getId())).thenReturn(Optional.of(student));
        StudentsRequestDto request = new StudentsRequestDto("https://cdn/recto.png", "https://cdn/verso.png", LicenseCategory.C);

        StudentsResponseDto dto = service.update(student.getId(), request);

        assertEquals(LicenseCategory.C, student.getLicenseCategory());
        assertEquals("https://cdn/recto.png", student.getCniRectoUrl());
        assertEquals("https://cdn/verso.png", student.getCniVersoUrl());
        assertEquals(LicenseCategory.C, dto.licenseCategory());
    }

    @Test
    void update_UnknownStudent_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(studentsRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(unknown, new StudentsRequestDto(null, null, LicenseCategory.B)));
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    /** Suppression LOGIQUE : la ligne reste en base, seul le drapeau deleted change. */
    @Test
    void delete_ShouldMarkStudentAsDeleted() {
        when(studentsRepository.findById(student.getId())).thenReturn(Optional.of(student));

        service.delete(student.getId());

        assertTrue(student.isDeleted());
        assertNotNull(student.getDeletedAt());
    }
}
