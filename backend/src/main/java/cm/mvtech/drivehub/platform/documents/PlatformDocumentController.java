package cm.mvtech.drivehub.platform.documents;

import cm.mvtech.drivehub.modules.document.application.controller.DocumentHttp;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentResponse;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentReviewRequest;
import cm.mvtech.drivehub.modules.document.domain.services.DocumentService;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Justificatifs vus depuis le back-office : l'équipe DriveHub vérifie la pièce d'identité et le CAPEC
 * du fondateur avant d'approuver une auto-école. Chaque consultation de fichier est journalisée.
 */
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROOT') || hasRole('SUPER_ADMIN') || hasRole('REVIEWER')")
@Tag(name = "PLATFORM JUSTIFICATIFS", description = "Vérification des justificatifs des fondateurs d'auto-école")
public class PlatformDocumentController {

    private final DocumentService documentService;
    private final DrivingSchoolRegistryRepository registryRepository;

    @Operation(summary = "Justificatifs du fondateur d'une auto-école")
    @GetMapping("/registries/{registryId}/documents")
    public List<DocumentResponse> registryDocuments(@PathVariable UUID registryId) {
        DrivingSchoolRegistry registry = registryRepository.findById(registryId)
                .orElseThrow(() -> new ResourceNotFoundException("Auto-école introuvable"));
        return documentService.list(registry.getAdmin());
    }

    @Operation(summary = "Voir un justificatif", description = "Flux binaire ; la consultation est journalisée")
    @GetMapping("/documents/{documentId}/file")
    public ResponseEntity<byte[]> file(@PathVariable UUID documentId, Authentication authentication) {
        return DocumentHttp.file(documentService.read(documentService.get(documentId),
                authentication.getName(), "PLATFORM_ADMIN", "SCHOOL_REVIEW"));
    }

    @Operation(summary = "Valider ou refuser un justificatif",
            description = "status : VERIFIED ou REJECTED (commentaire obligatoire en cas de refus, envoyé par email)")
    @PatchMapping("/documents/{documentId}/review")
    public DocumentResponse review(@PathVariable UUID documentId, @Valid @RequestBody DocumentReviewRequest request,
                                   Authentication authentication) {
        return documentService.review(documentId, request.status(), request.comment(), authentication.getName());
    }
}
