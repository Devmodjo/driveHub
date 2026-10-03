package cm.mvtech.drivehub.modules.student.application.dto;

import cm.mvtech.drivehub.modules.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Date;

/**
 * Inscription d'un élève (POST /api/auth/register/student).
 *
 * <p>{@code acceptPrivacyPolicy} est obligatoire et doit valoir {@code true} : la personne accepte
 * la politique de confidentialité (collecte de ses données pour vérifier son identité et, pour un
 * moniteur, la validité de son établissement). La date et la version acceptées sont enregistrées
 * sur le compte (preuve du consentement, loi n° 2024/017 du 23 décembre 2024).</p>
 */
public record StudentRegisterRequest(
        @Schema(defaultValue = "John")
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String firstname,

        @Schema(defaultValue = "Doe")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String lastname,

        @Schema(defaultValue = "john.doe@gmail.com")
        @NotBlank(message = "L'adresse email est obligatoire")
        @Email(message = "L'adresse email n'est pas valide")
        @Size(max = 150, message = "L'adresse email ne doit pas dépasser 150 caractères")
        String email,

        @Schema(defaultValue = "Pass1234")
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        String password,

        @Schema(defaultValue = "+237689078576")
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

        @Schema(defaultValue = "Yaoundé")
        @NotBlank(message = "La ville de résidence est obligatoire")
        @Size(max = 100, message = "La ville de résidence ne doit pas dépasser 100 caractères")
        String residenceCity,

        @Schema(defaultValue = "1999-01-01")
        @NotNull(message = "La date de naissance est obligatoire")
        @Past(message = "La date de naissance doit être dans le passé")
        Date dateOfBirth,

        @Schema(defaultValue = "true", description = "Acceptation de la politique de confidentialité (obligatoire)")
        @AssertTrue(message = "Vous devez accepter la politique de confidentialité pour créer un compte")
        boolean acceptPrivacyPolicy
) {}
