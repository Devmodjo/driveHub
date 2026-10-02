package cm.mvtech.drivehub.modules.document.application.controller;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentRequirements;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentResponse;
import cm.mvtech.drivehub.modules.document.domain.model.DocumentType;
import cm.mvtech.drivehub.modules.document.domain.services.DocumentService;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Justificatifs de l'utilisateur connecté (moniteur ou élève). Données du schéma public :
 * pas besoin d'en-tête X-Tenant-ID. Chaque utilisateur ne voit que ses propres documents.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
@Tag(name = "JUSTIFICATIFS", description = "Pièce d'identité (tous) et CAPEC (moniteurs), chiffrés")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    @Operation(summary = "Mes justificatifs")
    @GetMapping("/me")
    public List<DocumentResponse> mine(Authentication authentication) {
        return documentService.list(currentUser(authentication));
    }

    @Operation(summary = "Justificatifs demandés et manquants", description = "Élève : CNI ; moniteur : CNI et CAPEC")
    @GetMapping("/requirements")
    public DocumentRequirements requirements(Authentication authentication) {
        return documentService.requirements(currentUser(authentication));
    }

    @Operation(summary = "Envoyer un justificatif",
            description = "multipart/form-data : type (CNI ou CAPEC), file (PDF, JPEG, PNG ou WebP, 5 Mo max), "
                    + "documentNumber (facultatif). Remplace le document existant du même type.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestParam("type") DocumentType type,
                                                   @RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "documentNumber", required = false) String documentNumber,
                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(currentUser(authentication), type, file, documentNumber));
    }

    @Operation(summary = "Voir un de mes justificatifs")
    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> file(@PathVariable UUID id, Authentication authentication) {
        User user = currentUser(authentication);
        return DocumentHttp.file(documentService.read(documentService.getOwnedBy(user, id),
                user.getEmail(), "OWNER", "SELF"));
    }

    @Operation(summary = "Supprimer un de mes justificatifs", description = "Impossible s'il est déjà vérifié (409)")
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable UUID id, Authentication authentication) {
        documentService.deleteOwn(currentUser(authentication), id);
        return new ApiResponse(true, "Justificatif supprimé");
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
    }
}
