package cm.mvtech.drivehub.modules.exam.domain.services;

import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.InscriptionStatus;
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
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.domain.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Sessions d'examen et inscriptions des élèves. */
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamsRepository examsRepository;
    private final ExamsInscriptionRepository inscriptionRepository;
    private final ExamsMapper examsMapper;
    private final ExamsInscriptionMapper inscriptionMapper;
    private final StudentService studentService;
    private final CurrentSchoolProvider currentSchoolProvider;

    // ------------------------------------------------------------------ sessions

    @Transactional(readOnly = true)
    public ApiPageResponse<ExamsResponseDto> list(Pageable pageable) {
        return ApiPageResponse.from(examsRepository.findAllBy(pageable).map(examsMapper::fromEntityToResponse));
    }

    @Transactional
    public ExamsResponseDto create(ExamsRequestDto request) {
        Exam exam = new Exam();
        examsMapper.updateEntity(request, exam);
        exam.setDrivingSchool(currentSchoolProvider.get());
        return examsMapper.fromEntityToResponse(examsRepository.save(exam));
    }

    @Transactional
    public ExamsResponseDto update(UUID id, ExamsRequestDto request) {
        Exam exam = getExam(id);
        examsMapper.updateEntity(request, exam);
        return examsMapper.fromEntityToResponse(exam);
    }

    @Transactional
    public void delete(UUID id) {
        getExam(id).markDeleted();
    }

    // ------------------------------------------------------------------ inscriptions

    /**
     * Inscrit un élève à un examen.
     * Règles : pas de double inscription ; la catégorie de permis de l'élève doit correspondre à l'examen.
     */
    @Transactional
    public ExamsInscriptionResponseDto register(UUID examId, UUID studentId) {
        Exam exam = getExam(examId);
        Student student = studentService.getEntity(studentId);

        if (inscriptionRepository.existsByExams_IdAndStudent_Id(examId, studentId)) {
            throw new ConflictException("Cet élève est déjà inscrit à cet examen");
        }
        if (exam.getCategory() != null && student.getLicenseCategory() != null
                && exam.getCategory() != student.getLicenseCategory()) {
            throw new BadRequestException("L'élève prépare le permis " + student.getLicenseCategory()
                    + " mais l'examen concerne le permis " + exam.getCategory());
        }

        ExamsInscription inscription = new ExamsInscription();
        inscription.setExams(exam);
        inscription.setStudent(student);
        inscription.setInscriptionStatus(InscriptionStatus.INSCRIT);
        return inscriptionMapper.fromEntityToResponse(inscriptionRepository.save(inscription));
    }

    @Transactional
    public ExamsInscriptionResponseDto updateInscriptionStatus(UUID inscriptionId, InscriptionStatus status) {
        ExamsInscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription introuvable : " + inscriptionId));
        inscription.setInscriptionStatus(status);
        return inscriptionMapper.fromEntityToResponse(inscription);
    }

    @Transactional(readOnly = true)
    public List<ExamsInscriptionResponseDto> inscriptionsOfExam(UUID examId) {
        getExam(examId);
        return inscriptionRepository.findAllByExams_IdOrderByRegisteredAtAsc(examId).stream()
                .map(inscriptionMapper::fromEntityToResponse)
                .toList();
    }

    /** Inscriptions de l'élève connecté. */
    @Transactional(readOnly = true)
    public List<ExamsInscriptionResponseDto> myInscriptions() {
        Student me = studentService.currentStudent();
        return inscriptionRepository.findAllByStudent_IdOrderByRegisteredAtDesc(me.getId()).stream()
                .map(inscriptionMapper::fromEntityToResponse)
                .toList();
    }

    private Exam getExam(UUID id) {
        return examsRepository.findById(id)
                .filter(exam -> !exam.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Examen introuvable : " + id));
    }
}
