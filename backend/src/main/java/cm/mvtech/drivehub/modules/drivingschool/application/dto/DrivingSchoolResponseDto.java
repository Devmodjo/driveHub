package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;


public record DrivingSchoolResponseDto(
        @Schema(defaultValue = "1")
        UUID id,

        @Schema(defaultValue = "DRIVING SCHOOL")
        String name,

        @Schema(defaultValue = "678901013")
        String phoneNumber,

        @Schema(defaultValue = "Yaoundé, Nkoabang BP 456")
        String address,

        String email,

        String country,

        String city,

        LocalDate createdAt,

        String whatsappNumber,
        String websiteUrl,
        DrivingSchoolStatus drivingSchoolStatus,

        @Schema(defaultValue = "auto-ecole-le-volant-douala", description = "Adresse de la page publique : /auto-ecoles/{slug}")
        String slug,

        @Schema(description = "Présentation de l'auto-école (affichée sur sa page publique)")
        String description
) {
}
