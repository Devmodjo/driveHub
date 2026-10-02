package cm.mvtech.drivehub.modules.vehicle.domain.services;

import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesRequestDto;
import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesResponseDto;
import cm.mvtech.drivehub.modules.vehicle.domain.model.Vehicle;
import cm.mvtech.drivehub.modules.vehicle.infrastructure.mapper.VehiclesMapper;
import cm.mvtech.drivehub.modules.vehicle.infrastructure.repository.VehiclesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Flotte de véhicules de l'auto-école courante. */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehiclesRepository vehiclesRepository;
    private final VehiclesMapper mapper;
    private final CurrentSchoolProvider currentSchoolProvider;

    @Transactional(readOnly = true)
    public ApiPageResponse<VehiclesResponseDto> list(Pageable pageable) {
        return ApiPageResponse.from(vehiclesRepository.findAllBy(pageable).map(mapper::fromEntityToResponse));
    }

    @Transactional(readOnly = true)
    public VehiclesResponseDto get(UUID id) {
        return mapper.fromEntityToResponse(getEntity(id));
    }

    @Transactional
    public VehiclesResponseDto create(VehiclesRequestDto request) {
        VehiclesRequestDto normalized = normalize(request);
        if (vehiclesRepository.existsByMatriculationIgnoreCase(normalized.matriculation())) {
            throw new ConflictException("Un véhicule avec cette immatriculation existe déjà");
        }
        Vehicle vehicle = new Vehicle();
        mapper.updateEntity(normalized, vehicle);
        vehicle.setDrivingSchool(currentSchoolProvider.get());
        return mapper.fromEntityToResponse(vehiclesRepository.save(vehicle));
    }

    @Transactional
    public VehiclesResponseDto update(UUID id, VehiclesRequestDto request) {
        VehiclesRequestDto normalized = normalize(request);
        if (vehiclesRepository.existsByMatriculationIgnoreCaseAndIdNot(normalized.matriculation(), id)) {
            throw new ConflictException("Un autre véhicule utilise déjà cette immatriculation");
        }
        Vehicle vehicle = getEntity(id);
        mapper.updateEntity(normalized, vehicle);
        return mapper.fromEntityToResponse(vehicle);
    }

    @Transactional
    public void delete(UUID id) {
        getEntity(id).markDeleted();
    }

    public Vehicle getEntity(UUID id) {
        return vehiclesRepository.findById(id)
                .filter(vehicle -> !vehicle.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable : " + id));
    }

    /** "lt 123 ab" -> "LT 123 AB" : une seule écriture, pour comparer et stocker. */
    private VehiclesRequestDto normalize(VehiclesRequestDto request) {
        String matriculation = request.matriculation().trim().replaceAll("\\s+", " ").toUpperCase();
        return new VehiclesRequestDto(matriculation, request.model().trim(), request.state());
    }
}
