package cm.mvtech.drivehub.modules.vehicle;

import cm.mvtech.drivehub.modules.enums.State;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolResponseDto;

import java.util.UUID;

public record VehiclesResponseDto (
        UUID id,
        DrivingSchoolResponseDto drivingSchool,
        String matriculation,
        String model,
        State state
){
}
