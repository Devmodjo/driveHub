package cm.mvtech.drivehub.modules.drivingschool.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;


public record DrivingSchoolResponseDto(
        @Schema(defaultValue = "1")
        Long id,

        @Schema(defaultValue = "DRIVING SCHOOL")
        String name,

        @Schema(defaultValue = "678901013")
        String phoneNumber,

        @Schema(defaultValue = "Yaoundé, Nkoabang")
        String address,

        @Schema(defaultValue = "optionel mais essentiel pour attirer plus de prospect")
        String description,

        LocalDate createdAt
) {
}
