package cm.mvtech.drivehub.modules.course.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création / modification d'un cours (l'auto-école est celle du tenant). */
public record CoursesRequestDto(
        @NotBlank(message = "le titre du cours est obligatoire")
        String title,
        @NotBlank(message = "le contenu du cours est obligatoire")
        String content
) {
}
