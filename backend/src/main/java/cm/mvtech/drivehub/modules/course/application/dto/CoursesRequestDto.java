package cm.mvtech.drivehub.modules.course.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Création ou modification d'un cours théorique (colonne content en TEXT : migration tenant V5). */
public record CoursesRequestDto(
        @NotBlank(message = "Le titre du cours est obligatoire")
        @Size(max = 200, message = "Le titre du cours ne doit pas dépasser 200 caractères")
        String title,

        @NotBlank(message = "Le contenu du cours est obligatoire")
        @Size(max = 20000, message = "Le contenu du cours ne doit pas dépasser 20 000 caractères")
        String content
) {
}
