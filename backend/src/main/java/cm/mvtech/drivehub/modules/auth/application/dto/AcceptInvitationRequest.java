package cm.mvtech.drivehub.modules.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Le moniteur invité choisit son mot de passe et accepte la politique de confidentialité. */
public record AcceptInvitationRequest(
        @NotBlank(message = "Le lien d'invitation est incomplet")
        String token,

        @Schema(defaultValue = "Pass1234")
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        String password,

        @Schema(defaultValue = "true")
        @AssertTrue(message = "Vous devez accepter la politique de confidentialité pour activer votre compte")
        boolean acceptPrivacyPolicy
) {}
