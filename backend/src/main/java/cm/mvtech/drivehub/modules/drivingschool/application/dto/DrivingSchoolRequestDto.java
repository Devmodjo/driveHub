package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Demande de création d'une auto-école (POST /api/driving-schools/request).
 *
 * <p>Chaque champ a une longueur maximale : sans ces contrôles, une valeur trop longue
 * faisait échouer l'insertion en base (« value too long for type character varying(255) »)
 * avec un message incompréhensible pour l'utilisateur. Le message de chaque contrôle est
 * renvoyé tel quel au frontend, sous le champ concerné.</p>
 */
public record DrivingSchoolRequestDto(

        @Schema(defaultValue = "Auto-école l'Excellence")
        @NotBlank(message = "Le nom de l'établissement est obligatoire")
        @Size(max = 150, message = "Le nom de l'établissement ne doit pas dépasser 150 caractères")
        String name,

        @Schema(defaultValue = "excellence@gmail.com")
        @Email(message = "L'adresse email de l'établissement n'est pas valide")
        @Size(max = 150, message = "L'adresse email ne doit pas dépasser 150 caractères")
        String email,

        @Schema(defaultValue = "Cameroun")
        @Size(max = 100, message = "Le pays ne doit pas dépasser 100 caractères")
        String country,

        @Schema(defaultValue = "Yaoundé")
        @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
        String city,

        @Schema(defaultValue = "+237678901013")
        @NotBlank(message = "Le téléphone de l'établissement est obligatoire")
        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "Le téléphone doit contenir 8 à 20 chiffres (ex : +237 6XX XX XX XX)")
        String phoneNumber,

        @Schema(defaultValue = "Yaoundé, Bastos BP 441")
        @NotBlank(message = "L'adresse de l'établissement est obligatoire")
        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String address,

        @Schema(defaultValue = "Présentation de l'auto-école (facultative mais recommandée)")
        @Size(max = 2000, message = "La présentation ne doit pas dépasser 2000 caractères")
        String description,

        @Schema(defaultValue = "https://example.com")
        @Size(max = 255, message = "L'adresse du site web ne doit pas dépasser 255 caractères")
        @Pattern(regexp = "^$|^https?://.+", message = "L'adresse du site web doit commencer par http:// ou https://")
        String websiteUrl,

        @Schema(defaultValue = "+237678901013")
        @Pattern(regexp = "^$|^\\+?[0-9 ]{8,20}$", message = "Le numéro WhatsApp doit contenir 8 à 20 chiffres")
        String whatsappNumber
) {
}
