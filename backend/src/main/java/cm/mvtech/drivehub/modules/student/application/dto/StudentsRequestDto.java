package cm.mvtech.drivehub.modules.student.application.dto;

import cm.mvtech.drivehub.modules.enums.LicenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Mise à jour du dossier d'un élève par le moniteur.
 * Correction : {@code @NotBlank}/{@code @Positive} ne s'appliquent qu'aux textes / nombres ;
 * sur un enum ou un UUID ils provoquaient une erreur à la validation.
 * L'élève et l'auto-école sont déduits de l'URL et du tenant : plus d'userId ici.
 */
public record StudentsRequestDto(
        @Size(max = 255) String cniRectoUrl,
        @Size(max = 255) String cniVersoUrl,
        @NotNull(message = "la categorie du permis est obligatoire")
        LicenseCategory licenseCategory
) {
}
