package cm.mvtech.drivehub.modules.document.domain.services;

import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentResponse;
import cm.mvtech.drivehub.modules.document.domain.model.DocumentFile;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.domain.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Accès du RESPONSABLE d'une auto-école aux justificatifs de ses élèves (routes du tenant).
 *
 * <p>Minimisation des données : seul le responsable (le moniteur qui a créé l'auto-école) voit les pièces
 * d'identité, et uniquement celles des élèves de SON auto-école. Les moniteurs salariés n'y ont pas accès.
 * Chaque consultation est journalisée (contexte STUDENT_FILE).</p>
 */
@Service
@RequiredArgsConstructor
public class SchoolDocumentAccessService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final DrivingSchoolRegistryRepository registryRepository;
    private final StudentService studentService;
    private final DocumentService documentService;

    @Transactional(readOnly = true)
    public List<DocumentResponse> studentDocuments(UUID studentId) {
        requireOwnerOfCurrentSchool();
        return documentService.list(studentUser(studentId));
    }

    @Transactional
    public DocumentFile studentDocumentFile(UUID studentId, UUID documentId) {
        User owner = requireOwnerOfCurrentSchool();
        User student = studentUser(studentId);
        return documentService.read(documentService.getOwnedBy(student, documentId),
                owner.getEmail(), "SCHOOL_OWNER", "STUDENT_FILE");
    }

    /**
     * Le moniteur connecté doit être le responsable de l'auto-école du tenant courant (X-Tenant-ID).
     *
     * @return le compte du responsable
     */
    @Transactional(readOnly = true)
    public User requireOwnerOfCurrentSchool() {
        User user = userRepository.findById(currentUserProvider.get().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        String tenant = TenantContext.getTenantId();
        boolean owner = registryRepository.findByAdmin(user)
                .map(DrivingSchoolRegistry::getSchemaName)
                .filter(schema -> schema.equals(tenant))
                .isPresent();
        if (!owner) {
            throw new AccessDeniedException("Seul le responsable de l'auto-école peut consulter ou ajouter ces informations");
        }
        return user;
    }

    /** Élève de l'auto-école courante (404 s'il n'en fait pas partie : le dépôt lit le schéma du tenant). */
    private User studentUser(UUID studentId) {
        Student student = studentService.getEntity(studentId);
        return student.getUser();
    }
}
