package cm.mvtech.drivehub.modules.document.application.dto;

import cm.mvtech.drivehub.modules.document.domain.model.DocumentType;

import java.util.List;

/**
 * Justificatifs demandés à l'utilisateur connecté.
 *
 * @param required les types demandés selon son rôle (élève : CNI ; moniteur : CNI et CAPEC)
 * @param missing  ceux qui manquent encore (jamais envoyés, ou refusés)
 */
public record DocumentRequirements(List<DocumentType> required, List<DocumentType> missing) {
}
