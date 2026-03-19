package cm.mvtech.drivehub.modules.reservation;

import cm.mvtech.drivehub.modules.vehicle.ReservationsRequestDto;
import cm.mvtech.drivehub.modules.vehicle.ReservationsResponseDto;
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