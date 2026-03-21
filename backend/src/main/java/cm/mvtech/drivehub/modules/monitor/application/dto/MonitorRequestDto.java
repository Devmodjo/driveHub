package cm.mvtech.drivehub.modules.monitor.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record MonitorRequestDto(

        @Positive
        @NotBlank(message = "l'identifiant de l'utilisateur est obligatoire")
        UUID userId,

        @Positive
        @NotBlank(message = "l'identifiant de l'école est obligatoire")
        UUID drivingSchoolId,

        @NotBlank(message = "le contact de l'encadreur est obligatoire")
        String phoneNumber
) {
}
