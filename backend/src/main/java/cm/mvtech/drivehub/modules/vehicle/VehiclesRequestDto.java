package cm.mvtech.drivehub.modules.vehicle;

import cm.mvtech.drivehub.modules.enums.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record VehiclesRequestDto(

        @Positive
        @NotBlank(message = "l'identifiant de l'auto-ecole est obligatoire")
        UUID drivingSchoolId,

        @NotBlank(message = "l'immatriculation du vehicule est obligatoire")
        String matriculation,

        @NotBlank(message = "le model du vehicule est obligaoire")
        String model,

        @NotBlank(message = "l'état du vehicule est obligatoire")
        State state
) {
}
