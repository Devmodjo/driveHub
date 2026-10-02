package cm.mvtech.drivehub.modules.document.infrastructure.repository;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.document.domain.model.DocumentStatus;
import cm.mvtech.drivehub.modules.document.domain.model.DocumentType;
import cm.mvtech.drivehub.modules.document.domain.model.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDocumentRepository extends JpaRepository<UserDocument, UUID> {

    List<UserDocument> findAllByUserOrderByTypeAsc(User user);

    Optional<UserDocument> findByUserAndType(User user, DocumentType type);

    List<UserDocument> findAllByUserAndStatus(User user, DocumentStatus status);

    /** Le même numéro (empreinte) est-il déjà utilisé par un autre compte ? */
    boolean existsByTypeAndDocumentNumberHashAndUserNot(DocumentType type, String documentNumberHash, User user);
}
