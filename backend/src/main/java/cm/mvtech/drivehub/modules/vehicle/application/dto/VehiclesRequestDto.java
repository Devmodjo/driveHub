package cm.mvtech.drivehub.modules.vehicle.application.dto;

import cm.mvtech.drivehub.modules.enums.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Création / modification d'un véhicule. L'auto-école est celle du tenant (X-Tenant-ID). */
public record VehiclesRequestDto(
        @NotBlank(message = "l'immatriculation du vehicule est obligatoire")
        @Size(max = 20) String matriculation,
        @NotBlank(message = "le modele du vehicule est obligatoire")
        @Size(max = 100, message = "Le modèle du véhicule ne doit pas dépasser 100 caractères")
        String model,
        @NotNull(message = "l'état du vehicule est obligatoire")
        State state
) {
}
