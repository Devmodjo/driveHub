package cm.mvtech.drivehub.platform.mailing.application.dto;

import cm.mvtech.drivehub.platform.mailing.domain.model.EmailAudience;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Envoi d'un email depuis le back-office (POST /api/platform/emails).
 *
 * @param audience   à qui écrire (voir {@link EmailAudience})
 * @param schoolId   identifiant de l'auto-école (registre) : obligatoire si audience = SCHOOL_MEMBERS
 * @param recipients adresses choisies : obligatoire si audience = INDIVIDUAL (1 à 50)
 * @param subject    objet de l'email
 * @param message    texte brut ; les retours à la ligne sont conservés dans l'email
 */
public record SendPlatformEmailRequest(
        @NotNull(message = "Choisissez les destinataires")
        EmailAudience audience,

        UUID schoolId,

        @Size(max = 50, message = "50 destinataires au maximum par envoi individuel")
        List<@Email(message = "Une des adresses email n'est pas valide") String> recipients,

        @Schema(defaultValue = "Nouveauté sur DriveHub")
        @NotBlank(message = "L'objet est obligatoire")
        @Size(min = 3, max = 150, message = "L'objet doit contenir entre 3 et 150 caractères")
        String subject,

        @Schema(defaultValue = "Bonjour,\nNous avons le plaisir de vous annoncer...")
        @NotBlank(message = "Le message est obligatoire")
        @Size(min = 10, max = 5000, message = "Le message doit contenir entre 10 et 5000 caractères")
        String message
) {
}
