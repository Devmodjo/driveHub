package cm.mvtech.drivehub.modules.vehicle.infrastructure.mapper;

import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesRequestDto;
import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesResponseDto;
import cm.mvtech.drivehub.modules.vehicle.domain.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/** {@code updateEntity} sert à la création ET à la modification (une seule définition des champs copiés). */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehiclesMapper {

    void updateEntity(VehiclesRequestDto request, @MappingTarget Vehicle vehicle);

    VehiclesResponseDto fromEntityToResponse(Vehicle vehicle);
}
