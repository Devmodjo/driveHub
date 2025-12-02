package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.Vehicle;
import cm.drivemaster.backend.models.dto.VehiclesRequestDto;
import cm.drivemaster.backend.models.dto.VehiclesResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VehiclesMapper {
    VehiclesMapper INSTANCE = Mappers.getMapper(VehiclesMapper.class);

    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    Vehicle fromRequestToEntity(VehiclesRequestDto vehiclesRequestDto);
    VehiclesResponseDto fromEntityToResponse(Vehicle vehicles);
}