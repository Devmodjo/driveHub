package cm.mvtech.drivehub.modules.monitor.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record MonitorRequestDto(

        @Positive
        @NotBlank(message = "l'identifiant de l'utilisateur est obligatoire")
        Long userId,

        @Positive
        @NotBlank(message = "l'identifiant de l'école est obligatoire")
        Long drivingSchoolId,

        @NotBlank(message = "le contact de l'encadreur est obligatoire")
        String phoneNumber
) {
}
