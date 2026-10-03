package cm.mvtech.drivehub.platform.mailing.application.dto;

/**
 * Suggestion de destinataire pour l'envoi individuel (recherche par nom ou email).
 *
 * @param type MONITOR, STUDENT ou SCHOOL (adresse de contact d'une auto-école)
 */
public record RecipientSuggestion(String email, String fullName, String type, String schoolName) {
}
