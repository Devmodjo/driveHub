package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.PlatformAdmin;
import cm.drivemaster.backend.models.dto.PlatformAdminResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlatformAdminMapper {

    PlatformAdminResponse toResponse(PlatformAdmin platformAdmin);

    List<PlatformAdminResponse> toResponseList(List<PlatformAdmin> platformAdmins);
}