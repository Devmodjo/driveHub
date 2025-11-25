package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.Users;
import cm.drivemaster.backend.models.dto.UserRequestDto;
import cm.drivemaster.backend.models.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    Users fromRequestToEntity(UserRequestDto userRequestDto);
    UserResponseDto fromEntityToResponse(Users user);
}