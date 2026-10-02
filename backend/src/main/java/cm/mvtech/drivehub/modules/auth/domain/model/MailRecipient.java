package cm.mvtech.drivehub.modules.auth.domain.model;

/** Destinataire d'un email : adresse et nom affiché dans « Bonjour {name}, ». */
public record MailRecipient(String email, String name) {
}
