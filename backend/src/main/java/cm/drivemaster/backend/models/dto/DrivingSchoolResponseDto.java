package cm.drivemaster.backend.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;


public record DrivingSchoolResponseDto(
        @Schema(name = "1")
        Long id,

        @Schema(name = "DRIVING SCHOOL")
        String name,

        @Schema(name = "678901013")
        String phoneNumber,

        @Schema(name = "Yaoundé, Nkoabang")
        String address,

        @Schema(name = "optionel mais essentiel pour attirer plus de prospect")
        String description,

        LocalDate createdAt
) {
}
