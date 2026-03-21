package cm.mvtech.drivehub.modules.course.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CoursesRequestDto(

        @Positive
        @NotBlank(message = "l'identifiant de l'école est obligatoire")
        UUID drivingSchoolId,

        @NotBlank(message = "le titre du cours est obligatoire")
        String title,

        @NotBlank(message = "le contenu du cours est obligatoire")
        String content
) {
}
