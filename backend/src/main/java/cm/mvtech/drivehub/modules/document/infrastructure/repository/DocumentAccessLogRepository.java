package cm.mvtech.drivehub.modules.document.infrastructure.repository;

import cm.mvtech.drivehub.modules.document.domain.model.DocumentAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, UUID> {

    List<DocumentAccessLog> findAllByDocumentIdOrderByAccessedAtDesc(UUID documentId);
}
