package cm.mvtech.drivehub.modules.document.domain.model;

/** Contenu déchiffré d'un justificatif, prêt à être renvoyé au navigateur. */
public record DocumentFile(byte[] content, String contentType, String downloadName) {
}
