package cm.mvtech.drivehub.modules.monitor.application.dto;

import cm.mvtech.drivehub.modules.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.sql.Date;

/**
 * Moniteur ajouté par le responsable de l'auto-école (partie « monitor » de POST /api/monitors).
 *
 * <p>Pas de mot de passe ni de consentement ici : c'est le moniteur lui-même qui les donne en ouvrant
 * le lien d'invitation reçu par email (POST /api/auth/accept-invitation).</p>
 */
public record AddMonitorRequest(
        @Schema(defaultValue = "Paul")
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String firstname,

        @Schema(defaultValue = "Mbarga")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String lastname,

        @Schema(defaultValue = "paul.mbarga@gmail.com")
        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "L'adresse email n'est pas valide")
        @Size(max = 150, message = "L'adresse email ne doit pas dépasser 150 caractères")
        String email,

        @Schema(defaultValue = "+237690000000")
        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "Le téléphone doit contenir 8 à 20 chiffres (ex : +237 6XX XX XX XX)")
        String phoneNumber,

        @Schema(defaultValue = "MALE")
        @NotNull(message = "Le genre est obligatoire")
        Gender gender,

        @Schema(defaultValue = "Camerounaise")
        @NotBlank(message = "La nationalité est obligatoire")
        @Size(max = 100, message = "La nationalité ne doit pas dépasser 100 caractères")
        String nationality,

        @Schema(defaultValue = "Douala")
        @NotBlank(message = "La ville de résidence est obligatoire")
        @Size(max = 100, message = "La ville de résidence ne doit pas dépasser 100 caractères")
        String residenceCity,

        @Schema(defaultValue = "1990-01-01")
        @NotNull(message = "La date de naissance est obligatoire")
        @Past(message = "La date de naissance doit être dans le passé")
        Date dateOfBirth
) {}
