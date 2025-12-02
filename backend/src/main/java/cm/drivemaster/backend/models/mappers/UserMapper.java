package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.models.dto.UserRequestDto;
import cm.drivemaster.backend.models.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    User fromRequestToEntity(UserRequestDto userRequestDto);
    UserResponseDto fromEntityToResponse(User user);
}