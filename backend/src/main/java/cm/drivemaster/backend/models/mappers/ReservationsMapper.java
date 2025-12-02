package cm.drivemaster.backend.models.mappers;

import cm.drivemaster.backend.beans.Reservation;
import cm.drivemaster.backend.models.dto.ReservationsRequestDto;
import cm.drivemaster.backend.models.dto.ReservationsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReservationsMapper {
    ReservationsMapper INSTANCE = Mappers.getMapper(ReservationsMapper.class);

    @Mapping(source = "drivingSchoolId", target = "drivingSchool.id")
    @Mapping(source = "studentId", target = "student.id")
    @Mapping(source = "monitorId", target = "monitor.id")
    Reservation fromRequestToEntity(ReservationsRequestDto reservationsRequestDto);
    ReservationsResponseDto fromEntityToResponse(Reservation reservations);
}