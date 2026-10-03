package cm.mvtech.drivehub.modules.document.domain.services;

import cm.mvtech.drivehub.core.security.crypto.DataEncryptionService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Justificatifs (pièce d'identité, CAPEC) : envoi, consultation, vérification.
 *
 * <h2>Envoi d'un justificatif</h2>
 * <ol>
 *   <li>contrôles : type demandé pour ce rôle, fichier non vide, 5 Mo maximum, format réel accepté
 *       (signature binaire : PDF, JPEG, PNG, WebP) ;</li>
 *   <li>chiffrement du contenu avec une clé propre au fichier (chiffrement enveloppe) ;</li>
 *   <li>envoi du contenu chiffré au stockage (R2 en production) sous une clé aléatoire ;</li>
 *   <li>enregistrement en base (nom du fichier et numéro chiffrés) ; l'ancien justificatif du même type
 *       est supprimé une fois la transaction validée.</li>
 * </ol>
 *
 * <h2>Consultation</h2>
 * Toujours via {@link #read}, qui journalise qui consulte quoi, déchiffre et vérifie l'intégrité (SHA-256).
 * Les contrôles d'accès (« qui a le droit de voir ce document ? ») sont faits par l'appelant (contrôleur
 * ou service métier), au cas par cas : titulaire, responsable de l'auto-école, équipe DriveHub.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    /** Taille maximale d'un fichier (aussi limitée par spring.servlet.multipart.max-file-size). */
    public static final long MAX_FILE_BYTES = 5L * 1024 * 1024;

    private static final Pattern DOCUMENT_NUMBER = Pattern.compile("^[A-Za-z0-9 ./-]{3,50}$");
    private static final DateTimeFormatter KEY_MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    private final UserDocumentRepository documentRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final DocumentStorage storage;
    private final DataEncryptionService encryption;
    private final EmailService emailService;

    // ================================================================== règles

    /**
     * Justificatifs demandés selon le rôle : moniteur → pièce d'identité et CAPEC ; élève → aucun
     * (c'est l'auto-école qui l'accueille physiquement et vérifie son identité).
     */
    public static List<DocumentType> requiredTypes(Role role) {
        return role == Role.MONITOR ? List.of(DocumentType.CNI, DocumentType.CAPEC) : List.of();
    }

    /** Ce qui est demandé et ce qui manque encore (un justificatif refusé compte comme manquant). */
    @Transactional(readOnly = true)
    public DocumentRequirements requirements(User user) {
        List<DocumentType> required = requiredTypes(user.getRoles());
        List<DocumentType> provided = documentRepository.findAllByUserOrderByTypeAsc(user).stream()
                .filter(doc -> doc.getStatus() != DocumentStatus.REJECTED)
                .map(UserDocument::getType)
                .toList();
        List<DocumentType> missing = required.stream().filter(type -> !provided.contains(type)).toList();
        return new DocumentRequirements(required, missing);
    }

    /**
     * Bloque une action (demande de création d'auto-école, demande d'adhésion) tant qu'un justificatif manque.
     *
     * @param action fin de phrase, ex : « avant d'envoyer votre demande »
     */
    @Transactional(readOnly = true)
    public void assertRequiredDocuments(User user, String action) {
        List<DocumentType> missing = requirements(user).missing();
        if (!missing.isEmpty()) {
            List<String> labels = missing.stream().map(DocumentService::label).toList();
            throw new BadRequestException("Ajoutez " + String.join(" et ", labels) + " " + action
                    + " (page « Bienvenue » de votre espace).");
        }
    }

    public static String label(DocumentType type) {
        return type == DocumentType.CAPEC ? "votre CAPEC" : "votre pièce d'identité (CNI)";
    }

    // ================================================================== envoi

    /** Envoi par le titulaire lui-même : le justificatif est « en vérification ». */
    @Transactional
    public DocumentResponse upload(User owner, DocumentType type, MultipartFile file, String documentNumber) {
        return toResponse(store(owner, type, file, documentNumber, DocumentStatus.PENDING, null));
    }

    /**
     * Envoi par un tiers de confiance qui a vu l'original (le responsable d'une auto-école qui ajoute un moniteur) :
     * le justificatif est directement « vérifié » par cette personne.
     */
    @Transactional
    public UserDocument uploadVerified(User owner, DocumentType type, MultipartFile file, String documentNumber,
                                       String verifiedBy) {
        return store(owner, type, file, documentNumber, DocumentStatus.VERIFIED, verifiedBy);
    }

    private UserDocument store(User owner, DocumentType type, MultipartFile file, String documentNumber,
                               DocumentStatus status, String reviewer) {
        if (type == null) {
            throw new BadRequestException("Précisez le type de justificatif : CNI ou CAPEC");
        }
        if (!requiredTypes(owner.getRoles()).contains(type)) {
            throw new BadRequestException("Aucun justificatif n'est demandé aux élèves");
        }
        byte[] content = readContent(file);
        AllowedFileType fileType = AllowedFileType.detect(content).orElseThrow(() -> new BadRequestException(
                "Format de fichier non accepté : envoyez un PDF ou une photo (JPEG, PNG, WebP)"));
        String number = normalizeNumber(documentNumber);

        // 1. Chiffrement enveloppe : une clé par fichier, liée à son emplacement (objectKey)
        String objectKey = "documents/" + LocalDateTime.now().format(KEY_MONTH) + "/" + UUID.randomUUID();
        byte[] dataKey = encryption.newDataKey();
        byte[] encrypted = encryption.encryptFile(content, dataKey, objectKey);

        // 2. Stockage du contenu chiffré (retiré du stockage si la transaction est annulée plus loin)
        storage.put(objectKey, encrypted);
        deleteObjectOnRollback(objectKey);

        // 3. Enregistrement ; l'ancien justificatif du même type sera supprimé après validation
        UserDocument previous = documentRepository.findByUserAndType(owner, type).orElse(null);
        if (previous != null) {
            documentRepository.delete(previous);
            documentRepository.flush();   // libère la contrainte UNIQUE (user_id, type) avant l'insertion
            deleteObjectAfterCommit(previous.getObjectKey());
        }
        UserDocument document = new UserDocument();
        document.setUser(owner);
        document.setType(type);
        document.setStatus(status);
        document.setObjectKey(objectKey);
        document.setWrappedDataKey(encryption.wrapDataKey(dataKey));
        document.setContentType(fileType.contentType());
        document.setSizeBytes(content.length);
        document.setSha256(sha256(content));
        document.setFileName(cleanFileName(file.getOriginalFilename(), fileType));
        document.setDocumentNumber(number);
        document.setDocumentNumberHash(encryption.blindIndex(number));
        document.setUploadedAt(LocalDateTime.now());
        if (status == DocumentStatus.VERIFIED) {
            document.setReviewedAt(LocalDateTime.now());
            document.setReviewedBy(reviewer);
        }
        try {
            return documentRepository.save(document);
        } catch (RuntimeException e) {
            storage.delete(objectKey);   // pas de fichier orphelin si l'enregistrement échoue
            throw e;
        }
    }

    // ================================================================== consultation

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(User owner) {
        return documentRepository.findAllByUserOrderByTypeAsc(owner).stream().map(this::toResponse).toList();
    }

    /** Justificatif par identifiant ; 404 s'il n'existe pas. */
    @Transactional(readOnly = true)
    public UserDocument get(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Justificatif introuvable"));
    }

    /**
     * Justificatif appartenant à {@code owner} ; 404 sinon (on ne révèle pas l'existence d'un document
     * qui appartient à quelqu'un d'autre).
     */
    @Transactional(readOnly = true)
    public UserDocument getOwnedBy(User owner, UUID documentId) {
        return documentRepository.findById(documentId)
                .filter(doc -> doc.getUser().getId().equals(owner.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Justificatif introuvable"));
    }

    /**
     * Déchiffre un justificatif et journalise la consultation.
     * L'appelant doit avoir vérifié AVANT que {@code actorEmail} a le droit de le voir.
     *
     * @param actorRole OWNER, SCHOOL_OWNER ou PLATFORM_ADMIN
     * @param context   écran d'origine (SELF, JOIN_REQUEST, STUDENT_FILE, SCHOOL_REVIEW)
     */
    @Transactional
    public DocumentFile read(UserDocument document, String actorEmail, String actorRole, String context) {
        accessLogRepository.save(new DocumentAccessLog(null, document.getId(), actorEmail, actorRole, context,
                LocalDateTime.now()));

        byte[] dataKey = encryption.unwrapDataKey(document.getWrappedDataKey());
        byte[] content = encryption.decryptFile(storage.get(document.getObjectKey()), dataKey, document.getObjectKey());
        if (!sha256(content).equals(document.getSha256())) {
            log.error("Intégrité du justificatif {} non vérifiée", document.getId());
            throw new IllegalStateException("Le fichier est endommagé : demandez à l'utilisateur de l'envoyer de nouveau");
        }
        AllowedFileType fileType = AllowedFileType.fromContentType(document.getContentType())
                .orElseThrow(() -> new IllegalStateException("Format de fichier inconnu"));
        String downloadName = document.getType().name().toLowerCase() + "." + fileType.extension();
        return new DocumentFile(content, fileType.contentType(), downloadName);
    }

    // ================================================================== suppression et vérification

    /** Suppression par le titulaire (droit à l'effacement), sauf justificatif déjà vérifié. */
    @Transactional
    public void deleteOwn(User owner, UUID documentId) {
        UserDocument document = getOwnedBy(owner, documentId);
        if (document.getStatus() == DocumentStatus.VERIFIED) {
            throw new ConflictException("Un justificatif vérifié ne peut pas être supprimé : "
                    + "envoyez-en un nouveau pour le remplacer");
        }
        documentRepository.delete(document);
        deleteObjectAfterCommit(document.getObjectKey());
    }

    /** Passe à « vérifié » tous les justificatifs en attente d'un utilisateur (approbation d'une demande). */
    @Transactional
    public void markPendingAsVerified(User owner, String reviewer) {
        for (UserDocument document : documentRepository.findAllByUserAndStatus(owner, DocumentStatus.PENDING)) {
            document.setStatus(DocumentStatus.VERIFIED);
            document.setReviewedAt(LocalDateTime.now());
            document.setReviewedBy(reviewer);
            document.setReviewComment(null);
        }
    }

    /** Décision de l'équipe DriveHub sur un justificatif ; en cas de refus, l'utilisateur est prévenu par email. */
    @Transactional
    public DocumentResponse review(UUID documentId, DocumentStatus decision, String comment, String reviewer) {
        if (decision == DocumentStatus.PENDING) {
            throw new BadRequestException("La décision doit être VERIFIED ou REJECTED");
        }
        if (decision == DocumentStatus.REJECTED && (comment == null || comment.isBlank())) {
            throw new BadRequestException("Expliquez la raison du refus : elle sera envoyée à l'utilisateur");
        }
        UserDocument document = get(documentId);
        document.setStatus(decision);
        document.setReviewedAt(LocalDateTime.now());
        document.setReviewedBy(reviewer);
        document.setReviewComment(decision == DocumentStatus.REJECTED ? comment.trim() : null);
        if (decision == DocumentStatus.REJECTED) {
            User owner = document.getUser();
            emailService.sendDocumentRejectedEmail(owner.getEmail(), owner.getFirstname(),
                    label(document.getType()), comment.trim());
        }
        return toResponse(document);
    }

    // ================================================================== outils

    public DocumentResponse toResponse(UserDocument document) {
        boolean duplicate = document.getDocumentNumberHash() != null
                && documentRepository.existsByTypeAndDocumentNumberHashAndUserNot(
                document.getType(), document.getDocumentNumberHash(), document.getUser());
        return new DocumentResponse(document.getId(), document.getType(), document.getStatus(),
                mask(document.getDocumentNumber()), document.getFileName(), document.getContentType(),
                document.getSizeBytes(), document.getUploadedAt(), document.getReviewComment(), duplicate);
    }

    /** « 123 456 789 » → « ••••6789 » (4 derniers caractères, sans espaces ni séparateurs). */
    static String mask(String number) {
        if (number == null || number.isBlank()) {
            return null;
        }
        String compact = number.replaceAll("[\\s.\\-/]", "");
        return compact.length() <= 4 ? "••••" : "••••" + compact.substring(compact.length() - 4);
    }

    private static byte[] readContent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est vide : choisissez une photo ou un PDF du document");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new BadRequestException("Le fichier dépasse 5 Mo : réduisez la taille de la photo ou du PDF");
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Le fichier n'a pas pu être lu : réessayez");
        }
    }

    private static String normalizeNumber(String number) {
        if (number == null || number.isBlank()) {
            return null;
        }
        String trimmed = number.trim();
        if (!DOCUMENT_NUMBER.matcher(trimmed).matches()) {
            throw new BadRequestException("Numéro du document invalide : 3 à 50 caractères (lettres, chiffres, espaces, tirets)");
        }
        return trimmed;
    }

    /** Nom de fichier affiché : sans chemin, sans caractères spéciaux, 100 caractères maximum, extension réelle. */
    private static String cleanFileName(String original, AllowedFileType type) {
        String base = original == null ? "" : original.replaceAll(".*[/\\\\]", "");
        base = base.replaceAll("\\.[A-Za-z0-9]{1,5}$", "").replaceAll("[^\\p{L}\\p{N} ._-]", "").trim();
        if (base.isBlank()) {
            base = "document";
        }
        if (base.length() > 100) {
            base = base.substring(0, 100);
        }
        return base + "." + type.extension();
    }

    private void deleteObjectAfterCommit(String objectKey) {
        Runnable delete = () -> {
            try {
                storage.delete(objectKey);
            } catch (RuntimeException e) {
                log.warn("Ancien fichier {} non supprimé du stockage : {}", objectKey, e.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    delete.run();
                }
            });
        } else {
            delete.run();
        }
    }

    /** Le stockage ne participe pas à la transaction : on retire nous-mêmes le fichier si elle est annulée. */
    private void deleteObjectOnRollback(String objectKey) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        try {
                            storage.delete(objectKey);
                        } catch (RuntimeException e) {
                            log.warn("Fichier {} non supprimé après annulation : {}", objectKey, e.getMessage());
                        }
                    }
                }
            });
        }
    }

    static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
