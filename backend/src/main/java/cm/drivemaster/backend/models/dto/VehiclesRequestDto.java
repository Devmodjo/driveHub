package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.State;

public record VehiclesRequestDto(
        Long drivingSchoolId,
        String matriculation,
        String model,
        State state
) {
}
