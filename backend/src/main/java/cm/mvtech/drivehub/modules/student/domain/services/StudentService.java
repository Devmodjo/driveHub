package cm.mvtech.drivehub.modules.student.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.student.application.dto.StudentsRequestDto;
import cm.mvtech.drivehub.modules.student.application.dto.StudentsResponseDto;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.infrastructure.mapper.StudentsMapper;
import cm.mvtech.drivehub.modules.student.infrastructure.repository.StudentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Gestion des élèves de l'auto-école courante.
 * Toutes les requêtes s'exécutent dans le schéma choisi par X-Tenant-ID : l'isolation est
 * assurée par PostgreSQL (search_path), aucune clause "WHERE auto_ecole = ?" n'est nécessaire.
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentsRepository studentsRepository;
    private final StudentsMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public ApiPageResponse<StudentsResponseDto> list(Pageable pageable) {
        return ApiPageResponse.from(studentsRepository.findAllBy(pageable).map(mapper::fromEntityToResponse));
    }

    @Transactional(readOnly = true)
    public StudentsResponseDto get(UUID id) {
        return mapper.fromEntityToResponse(getEntity(id));
    }

    /** Fiche de l'élève connecté. */
    @Transactional(readOnly = true)
    public StudentsResponseDto me() {
        return mapper.fromEntityToResponse(currentStudent());
    }

    @Transactional
    public StudentsResponseDto update(UUID id, StudentsRequestDto request) {
        Student student = getEntity(id);
        student.setCniRectoUrl(request.cniRectoUrl());
        student.setCniVersoUrl(request.cniVersoUrl());
        student.setLicenseCategory(request.licenseCategory());
        return mapper.fromEntityToResponse(student);
    }

    /** Suppression logique : l'historique (paiements, examens) est conservé. */
    @Transactional
    public void delete(UUID id) {
        getEntity(id).markDeleted();
    }

    public Student getEntity(UUID id) {
        return studentsRepository.findById(id)
                .filter(student -> !student.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Élève introuvable : " + id));
    }

    /** Fiche élève (dans le tenant) de l'utilisateur connecté. */
    public Student currentStudent() {
        UUID userId = currentUserProvider.get().getId();
        return studentsRepository.findFirstByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun dossier élève pour ce compte dans cette auto-école"));
    }
}
