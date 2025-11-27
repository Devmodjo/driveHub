package cm.drivemaster.backend.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DrivingSchoolRequestDto(

        @Schema(name = "DRIVING SCHOOL")
        @NotBlank(message = "le nom de l'établissement est obligatoire")
        String name,

        @Schema(name = "678901013")
        @NotBlank(message = "le contact de l'établissement est obligatoire")
        String phoneNumber,

        @Schema(name = "Yaoundé, Nkoabang")
        @NotBlank(message = "l'addresse de l'établissement est obligatoire")
        String address,
        @Schema(name = "optionel mais essentiel pour attirer plus de prospect")
        String description
) {
}
