package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.Monitor;
import cm.drivemaster.backend.models.dto.MonitorRequestDto;
import cm.drivemaster.backend.models.dto.MonitorResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MonitorMapper {
    MonitorMapper INSTANCE = Mappers.getMapper(MonitorMapper.class);

    @Mapping(source = "userId", target = "users.id")
    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    Monitor fromRequestToEntity(MonitorRequestDto monitorRequestDto);
    MonitorResponseDto fromEntityToResponse(Monitor monitor);
}