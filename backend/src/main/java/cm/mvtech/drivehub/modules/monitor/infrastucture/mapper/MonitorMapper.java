package cm.mvtech.drivehub.modules.monitor.infrastucture.mapper;

import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorRequestDto;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MonitorMapper {
    MonitorMapper INSTANCE = Mappers.getMapper(MonitorMapper.class);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    Monitor fromRequestToEntity(MonitorRequestDto monitorRequestDto);
    MonitorResponseDto fromEntityToResponse(Monitor monitor);
}