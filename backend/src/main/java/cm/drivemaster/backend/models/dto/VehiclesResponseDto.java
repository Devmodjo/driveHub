package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.State;

public record VehiclesResponseDto (
        Long id,
        DrivingSchoolResponseDto drivingSchool,
        String matriculation,
        String model,
        State state
){
}
