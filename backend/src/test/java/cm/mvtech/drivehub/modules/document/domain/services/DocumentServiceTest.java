package cm.mvtech.drivehub.modules.document.domain.services;

import cm.mvtech.drivehub.core.security.crypto.DataEncryptionService;
import cm.mvtech.drivehub.core.security.crypto.TestEncryption;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentRequirements;
import cm.mvtech.drivehub.modules.document.application.dto.DocumentResponse;
import cm.mvtech.drivehub.modules.document.domain.model.*;
import cm.mvtech.drivehub.modules.document.infrastructure.repository.DocumentAccessLogRepository;
import cm.mvtech.drivehub.modules.document.infrastructure.repository.UserDocumentRepository;
import cm.mvtech.drivehub.modules.document.infrastructure.storage.DocumentStorage;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Règles des justificatifs : pièces demandées selon le rôle, contrôle des fichiers, chiffrement avant stockage,
 * remplacement, consultation journalisée, vérification et refus.
 * Le chiffrement est réel ; le stockage est une simple Map en mémoire.
 */
class DocumentServiceTest {

    /** Plus petite image PNG valide. */
    static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    private final UserDocumentRepository documentRepository = mock(UserDocumentRepository.class);
    private final DocumentAccessLogRepository accessLogRepository = mock(DocumentAccessLogRepository.class);
    private final EmailService emailService = mock(EmailService.class);
    private final Map<String, byte[]> bucket = new HashMap<>();
    private final DocumentStorage storage = new DocumentStorage() {
        @Override public void put(String key, byte[] content) { bucket.put(key, content); }
        @Override public byte[] get(String key) { return bucket.get(key); }
        @Override public void delete(String key) { bucket.remove(key); }
    };

    private DocumentService service;
    private DataEncryptionService encryption;
    private User monitor;
    private User student;

    @BeforeEach
    void setUp() {
        encryption = TestEncryption.service();
        service = new DocumentService(documentRepository, accessLogRepository, storage, encryption, emailService);
        monitor = user(Role.MONITOR, "moniteur@test.cm");
        student = user(Role.STUDENT, "eleve@test.cm");
        when(documentRepository.save(any())).thenAnswer(inv -> {
            UserDocument doc = inv.getArgument(0);
            if (doc.getId() == null) {
                doc.setId(UUID.randomUUID());
            }
            return doc;
        });
    }

    private static User user(Role role, String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRoles(role);
        user.setEmail(email);
        user.setFirstname("Awa");
        return user;
    }

    private static MockMultipartFile png(String name) {
        return new MockMultipartFile("file", name, "image/png", PNG);
    }

    // ------------------------------------------------------------------ règles

    @Test
    void requiredTypes_DependOnRole() {
        assertEquals(List.of(DocumentType.CNI, DocumentType.CAPEC), DocumentService.requiredTypes(Role.MONITOR));
        assertEquals(List.of(), DocumentService.requiredTypes(Role.STUDENT));
    }

    @Test
    void requirements_RejectedDocumentCountsAsMissing() {
        UserDocument cni = new UserDocument();
        cni.setType(DocumentType.CNI);
        cni.setStatus(DocumentStatus.REJECTED);
        when(documentRepository.findAllByUserOrderByTypeAsc(monitor)).thenReturn(List.of(cni));

        DocumentRequirements requirements = service.requirements(monitor);
        assertEquals(List.of(DocumentType.CNI, DocumentType.CAPEC), requirements.missing());
    }

    @Test
    void assertRequiredDocuments_MissingDocument_ShouldExplainWhatToDo() {
        when(documentRepository.findAllByUserOrderByTypeAsc(monitor)).thenReturn(List.of());
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> service.assertRequiredDocuments(monitor, "avant de rejoindre une auto-école"));
        assertTrue(e.getMessage().contains("avant de rejoindre une auto-école"));
        assertTrue(e.getMessage().contains("CAPEC"));
    }

    @Test
    void assertRequiredDocuments_Student_NothingRequired() {
        when(documentRepository.findAllByUserOrderByTypeAsc(student)).thenReturn(List.of());
        assertDoesNotThrow(() -> service.assertRequiredDocuments(student, "avant de rejoindre une auto-école"));
    }

    // ------------------------------------------------------------------ envoi

    @Test
    void upload_EncryptsBeforeStorage_AndStoresMetadataEncrypted() {
        DocumentResponse response = service.upload(monitor, DocumentType.CNI, png("../ma cni<script>.png"), "AB 123 456");

        assertEquals(DocumentStatus.PENDING, response.status());
        assertEquals("image/png", response.contentType());
        assertEquals("••••3456", response.documentNumberMasked(), "le numéro n'est jamais renvoyé en clair");
        assertEquals(1, bucket.size());
        byte[] stored = bucket.values().iterator().next();
        assertFalse(Arrays.equals(PNG, stored), "le stockage ne reçoit que du contenu chiffré");

        ArgumentCaptor<UserDocument> saved = ArgumentCaptor.forClass(UserDocument.class);
        verify(documentRepository).save(saved.capture());
        UserDocument doc = saved.getValue();
        assertEquals("ma cniscript.png", doc.getFileName(), "nom nettoyé (sans chemin ni caractères spéciaux)");
        assertEquals(encryption.blindIndex("AB123456"), doc.getDocumentNumberHash());
        assertTrue(doc.getObjectKey().matches("documents/\\d{4}/\\d{2}/[0-9a-f-]{36}"));
        assertNotNull(doc.getSha256());
    }

    @Test
    void upload_ByStudent_ShouldBeRefused() {
        assertThrows(BadRequestException.class, () -> service.upload(student, DocumentType.CAPEC, png("capec.png"), null));
        assertThrows(BadRequestException.class, () -> service.upload(student, DocumentType.CNI, png("cni.png"), null));
        assertTrue(bucket.isEmpty());
    }

    @Test
    void upload_InvalidFiles_ShouldBeRefusedWithPreciseMessage() {
        MockMultipartFile empty = new MockMultipartFile("file", "cni.png", "image/png", new byte[0]);
        MockMultipartFile html = new MockMultipartFile("file", "cni.png", "image/png",
                "<html><script>alert(1)</script></html>".getBytes());
        MockMultipartFile tooBig = new MockMultipartFile("file", "cni.pdf", "application/pdf",
                new byte[(int) DocumentService.MAX_FILE_BYTES + 1]);

        assertTrue(assertThrows(BadRequestException.class,
                () -> service.upload(monitor, DocumentType.CNI, empty, null)).getMessage().contains("vide"));
        assertTrue(assertThrows(BadRequestException.class,
                () -> service.upload(monitor, DocumentType.CNI, html, null)).getMessage().contains("Format"));
        assertTrue(assertThrows(BadRequestException.class,
                () -> service.upload(monitor, DocumentType.CNI, tooBig, null)).getMessage().contains("5 Mo"));
        assertThrows(BadRequestException.class,
                () -> service.upload(monitor, DocumentType.CNI, png("cni.png"), "<script>"));
        assertTrue(bucket.isEmpty());
    }

    @Test
    void upload_ReplacesPreviousDocumentOfSameType() {
        UserDocument previous = new UserDocument();
        previous.setObjectKey("documents/2020/01/old");
        bucket.put("documents/2020/01/old", new byte[]{1});
        when(documentRepository.findByUserAndType(monitor, DocumentType.CNI)).thenReturn(Optional.of(previous));

        service.upload(monitor, DocumentType.CNI, png("cni.png"), null);

        verify(documentRepository).delete(previous);
        verify(documentRepository).flush();
        assertFalse(bucket.containsKey("documents/2020/01/old"), "hors transaction : l'ancien fichier est supprimé aussitôt");
    }

    @Test
    void uploadVerified_IsMarkedVerifiedBy() {
        UserDocument doc = service.uploadVerified(monitor, DocumentType.CAPEC, png("capec.png"), null, "owner@test.cm");
        assertEquals(DocumentStatus.VERIFIED, doc.getStatus());
        assertEquals("owner@test.cm", doc.getReviewedBy());
        assertNotNull(doc.getReviewedAt());
    }

    // ------------------------------------------------------------------ consultation

    @Test
    void read_DecryptsOriginalContent_AndLogsAccess() {
        service.upload(monitor, DocumentType.CNI, png("cni.png"), null);
        ArgumentCaptor<UserDocument> saved = ArgumentCaptor.forClass(UserDocument.class);
        verify(documentRepository).save(saved.capture());

        DocumentFile file = service.read(saved.getValue(), "owner@test.cm", "SCHOOL_OWNER", "STUDENT_FILE");

        assertArrayEquals(PNG, file.content());
        assertEquals("image/png", file.contentType());
        assertEquals("cni.png", file.downloadName());
        ArgumentCaptor<DocumentAccessLog> log = ArgumentCaptor.forClass(DocumentAccessLog.class);
        verify(accessLogRepository).save(log.capture());
        assertEquals("owner@test.cm", log.getValue().getAccessedBy());
    }

    @Test
    void read_CorruptedFile_ShouldFail() {
        service.upload(monitor, DocumentType.CNI, png("cni.png"), null);
        ArgumentCaptor<UserDocument> saved = ArgumentCaptor.forClass(UserDocument.class);
        verify(documentRepository).save(saved.capture());
        byte[] stored = bucket.get(saved.getValue().getObjectKey());
        stored[stored.length - 1] ^= 1;

        assertThrows(IllegalStateException.class, () -> service.read(saved.getValue(), "a", "OWNER", "SELF"));
    }

    @Test
    void getOwnedBy_DocumentOfAnotherUser_Is404() {
        UserDocument doc = new UserDocument();
        doc.setUser(monitor);
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        assertThrows(ResourceNotFoundException.class, () -> service.getOwnedBy(student, id));
        assertSame(doc, service.getOwnedBy(monitor, id));
    }

    // ------------------------------------------------------------------ suppression et vérification

    @Test
    void deleteOwn_VerifiedDocument_IsConflict() {
        UserDocument doc = new UserDocument();
        doc.setUser(student);
        doc.setStatus(DocumentStatus.VERIFIED);
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        assertThrows(ConflictException.class, () -> service.deleteOwn(student, id));
        verify(documentRepository, never()).delete(any());
    }

    @Test
    void review_RejectWithoutReason_IsRefused_AndRejectSendsEmail() {
        UUID id = UUID.randomUUID();
        UserDocument doc = new UserDocument();
        doc.setId(id);
        doc.setUser(student);
        doc.setType(DocumentType.CNI);
        doc.setStatus(DocumentStatus.PENDING);
        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        assertThrows(BadRequestException.class, () -> service.review(id, DocumentStatus.REJECTED, " ", "admin"));
        assertThrows(BadRequestException.class, () -> service.review(id, DocumentStatus.PENDING, null, "admin"));

        DocumentResponse response = service.review(id, DocumentStatus.REJECTED, "Photo illisible", "admin@drivehub.cm");
        assertEquals(DocumentStatus.REJECTED, response.status());
        assertEquals("Photo illisible", response.reviewComment());
        verify(emailService).sendDocumentRejectedEmail(eq("eleve@test.cm"), eq("Awa"), anyString(), eq("Photo illisible"));
    }

    @Test
    void markPendingAsVerified() {
        UserDocument doc = new UserDocument();
        doc.setStatus(DocumentStatus.PENDING);
        when(documentRepository.findAllByUserAndStatus(student, DocumentStatus.PENDING)).thenReturn(List.of(doc));

        service.markPendingAsVerified(student, "owner@test.cm");

        assertEquals(DocumentStatus.VERIFIED, doc.getStatus());
        assertEquals("owner@test.cm", doc.getReviewedBy());
    }

    @Test
    void mask() {
        assertEquals("••••6789", DocumentService.mask("123456789"));
        assertEquals("••••", DocumentService.mask("123"));
        assertNull(DocumentService.mask(null));
    }
}
