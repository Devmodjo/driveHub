package cm.mvtech.drivehub.modules.auth.infrastructure.mapper;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.application.dto.UserRequestDto;
import cm.mvtech.drivehub.modules.auth.application.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    User fromRequestToEntity(UserRequestDto userRequestDto);
    UserResponseDto fromEntityToResponse(User user);
}