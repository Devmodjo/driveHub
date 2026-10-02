package cm.mvtech.drivehub.modules.monitor.infrastructure.mapper;

import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonitorMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstname", target = "firstname")
    @Mapping(source = "user.lastname", target = "lastname")
    @Mapping(source = "user.email", target = "email")
    MonitorResponseDto fromEntityToResponse(Monitor monitor);
}
