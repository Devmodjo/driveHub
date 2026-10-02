package cm.mvtech.drivehub.modules.reservation.infrastructure.mapper;

import cm.mvtech.drivehub.modules.reservation.application.dto.ReservationsResponseDto;
import cm.mvtech.drivehub.modules.reservation.domain.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationsMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.user.firstname", target = "studentFirstname")
    @Mapping(source = "student.user.lastname", target = "studentLastname")
    @Mapping(source = "monitor.id", target = "monitorId")
    @Mapping(source = "monitor.user.firstname", target = "monitorFirstname")
    @Mapping(source = "monitor.user.lastname", target = "monitorLastname")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.matriculation", target = "vehicleMatriculation")
    ReservationsResponseDto fromEntityToResponse(Reservation reservation);
}
