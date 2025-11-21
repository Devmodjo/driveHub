package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.Role;
import cm.drivemaster.backend.enums.Statut;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record UserResponseDto(

        @Schema(name = "1")
        Long id,

        @Schema(name = "John")
        String name,

        @Schema(name = "john@example.com")
        String email,

        @Schema(name = "password123")
        String password,

        @Schema(name = "ADMIN")
        Role roles,

        @Schema(name = "PENDING")
        Statut statut,

        LocalDate createdAt
) {
}
