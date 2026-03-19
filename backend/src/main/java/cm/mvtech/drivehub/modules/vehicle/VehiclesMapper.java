package cm.mvtech.drivehub.modules.vehicle;

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