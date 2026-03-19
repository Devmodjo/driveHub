package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DrivingSchoolRequestDto(

        @Schema(defaultValue = "auto école l'excellence")
        @NotBlank(message = "le nom de l'établissement est obligatoire")
        String name,
        @Schema(defaultValue = "excelence@gmail.com")
        String email,
        @Schema(defaultValue = "Cameroon")
        String country,
        @Schema(defaultValue = "Yaoundé")
        String city,
        @Schema(defaultValue = "+237678901013")
        @NotBlank(message = "le contact de l'établissement est obligatoire")
        String phoneNumber,
        @Schema(defaultValue = "Yaoundé, Bastos BP 441")
        @NotBlank(message = "l'addresse de l'établissement est obligatoire")
        String address,
        @Schema(defaultValue = "optionel mais essentiel pour attirer plus de prospect")
        String description,
        @Schema(defaultValue = "https://example.com")
        String websiteUrl,
        @Schema(defaultValue = "+237678901013")
        String whatsappNumber

) {
}
