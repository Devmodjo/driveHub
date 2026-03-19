package cm.drivemaster.backend.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PlatformAdminLoginRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password

) {}
