package cm.mvtech.drivehub.modules.document.domain.model;

/** État de vérification d'un justificatif. */
public enum DocumentStatus {
    /** Envoyé, pas encore vérifié. */
    PENDING,
    /** Vérifié (par l'équipe DriveHub ou par le responsable de l'auto-école). */
    VERIFIED,
    /** Refusé (illisible, expiré...) : l'utilisateur doit en envoyer un nouveau. */
    REJECTED
}
