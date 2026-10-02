package cm.mvtech.drivehub.modules.student.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import cm.mvtech.drivehub.modules.document.domain.services.SchoolDocumentAccessService;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentResponse;
import cm.mvtech.drivehub.modules.document.application.controller.DocumentHttp;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import cm.mvtech.drivehub.modules.student.application.dto.StudentsRequestDto;
import cm.mvtech.drivehub.modules.student.application.dto.StudentsResponseDto;
import cm.mvtech.drivehub.modules.student.domain.services.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Élèves de l'auto-école (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "STUDENT API", description = "gestion des élèves de l'auto-école (tenant)")
public class StudentController {

    private final SchoolDocumentAccessService schoolDocumentAccessService;
    private final StudentService studentService;

    @Operation(summary = "liste paginée des élèves")
    @GetMapping
    @PreAuthorize("hasRole('MONITOR')")
    public ApiPageResponse<StudentsResponseDto> list(
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {
        return studentService.list(pageable);
    }

    @Operation(summary = "dossier de l'élève connecté")
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentsResponseDto me() {
        return studentService.me();
    }

    @Operation(summary = "détail d'un élève")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public StudentsResponseDto get(@PathVariable UUID id) {
        return studentService.get(id);
    }

    @Operation(summary = "mettre à jour le dossier (CNI, catégorie de permis)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public StudentsResponseDto update(@PathVariable UUID id, @Valid @RequestBody StudentsRequestDto request) {
        return studentService.update(id, request);
    }

    @Operation(summary = "retirer un élève (suppression logique)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MONITOR')")
    public ApiResponse delete(@PathVariable UUID id) {
        studentService.delete(id);
        return new ApiResponse(true, "Élève retiré de l'auto-école");
    }

    @Operation(summary = "Justificatifs d'un élève", description = "Réservé au responsable de l'auto-école")
    @GetMapping("/{id}/documents")
    @PreAuthorize("hasRole('MONITOR')")
    public List<DocumentResponse> documents(@PathVariable UUID id) {
        return schoolDocumentAccessService.studentDocuments(id);
    }

    @Operation(summary = "Voir un justificatif d'un élève", description = "Réservé au responsable ; consultation journalisée")
    @GetMapping("/{id}/documents/{documentId}/file")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<byte[]> documentFile(@PathVariable UUID id, @PathVariable UUID documentId) {
        return DocumentHttp.file(
                schoolDocumentAccessService.studentDocumentFile(id, documentId));
    }
}
