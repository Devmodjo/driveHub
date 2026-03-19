package cm.mvtech.drivehub.platform.admin.models.mappers;

import cm.mvtech.drivehub.platform.admin.models.PlatformAdmin;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlatformAdminMapper {

    PlatformAdminResponse toResponse(PlatformAdmin platformAdmin);

    List<PlatformAdminResponse> toResponseList(List<PlatformAdmin> platformAdmins);
}