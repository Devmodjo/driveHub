package cm.mvtech.drivehub.modules.vehicle.application.dto;

import cm.mvtech.drivehub.modules.enums.State;

import java.time.LocalDateTime;
import java.util.UUID;

public record VehiclesResponseDto(
        UUID id,
        String matriculation,
        String model,
        State state,
        LocalDateTime createdOn
) {
}
