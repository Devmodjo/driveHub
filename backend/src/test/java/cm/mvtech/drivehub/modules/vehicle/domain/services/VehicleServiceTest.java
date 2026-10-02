package cm.mvtech.drivehub.modules.vehicle.domain.services;

import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.State;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesRequestDto;
import cm.mvtech.drivehub.modules.vehicle.application.dto.VehiclesResponseDto;
import cm.mvtech.drivehub.modules.vehicle.domain.model.Vehicle;
import cm.mvtech.drivehub.modules.vehicle.infrastructure.mapper.VehiclesMapper;
import cm.mvtech.drivehub.modules.vehicle.infrastructure.repository.VehiclesRepository;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link VehicleService} (flotte de l'auto-école).
 *
 * <p>Point important : l'immatriculation est NORMALISÉE (majuscules, espaces uniques) avant
 * d'être comparée et enregistrée. « lt  452 ab » et « LT 452 AB » désignent donc le même véhicule.</p>
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehiclesRepository vehiclesRepository;
    @Mock private CurrentSchoolProvider currentSchoolProvider;
    @Spy  private VehiclesMapper mapper = Mappers.getMapper(VehiclesMapper.class);

    @InjectMocks
    private VehicleService service;

    private DrivingSchool school;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        school = TestData.school();
        vehicle = TestData.vehicle("LT 452 AB");
    }

    // ─── create ───────────────────────────────────────────────────────────────

    /** Cas nominal : immatriculation normalisée, auto-école du tenant renseignée, véhicule sauvegardé. */
    @Test
    void create_ShouldNormalizeMatriculationAndAttachSchool() {
        when(vehiclesRepository.existsByMatriculationIgnoreCase("LT 452 AB")).thenReturn(false);
        when(currentSchoolProvider.get()).thenReturn(school);
        when(vehiclesRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiclesResponseDto dto = service.create(new VehiclesRequestDto("  lt  452   ab ", " Toyota Yaris ", State.DISPOSABLE));

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehiclesRepository).save(captor.capture());
        assertEquals("LT 452 AB", captor.getValue().getMatriculation());
        assertEquals("Toyota Yaris", captor.getValue().getModel());
        assertSame(school, captor.getValue().getDrivingSchool());
        assertEquals("LT 452 AB", dto.matriculation());
        assertEquals(State.DISPOSABLE, dto.state());
    }

    /** Immatriculation déjà utilisée (même écrite autrement) : 409, rien n'est sauvegardé. */
    @Test
    void create_DuplicateMatriculation_ShouldThrowConflict() {
        when(vehiclesRepository.existsByMatriculationIgnoreCase("LT 452 AB")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.create(new VehiclesRequestDto("lt 452 ab", "Autre", State.DISPOSABLE)));
        verify(vehiclesRepository, never()).save(any());
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    void update_ShouldChangeStateAndModel() {
        when(vehiclesRepository.existsByMatriculationIgnoreCaseAndIdNot("LT 452 AB", vehicle.getId())).thenReturn(false);
        when(vehiclesRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        VehiclesResponseDto dto = service.update(vehicle.getId(),
                new VehiclesRequestDto("LT 452 AB", "Toyota Corolla", State.MAINTENANCE));

        assertEquals(State.MAINTENANCE, vehicle.getState());
        assertEquals("Toyota Corolla", vehicle.getModel());
        assertEquals(State.MAINTENANCE, dto.state());
    }

    /** Un AUTRE véhicule porte déjà la nouvelle immatriculation : 409. */
    @Test
    void update_MatriculationUsedByAnotherVehicle_ShouldThrowConflict() {
        when(vehiclesRepository.existsByMatriculationIgnoreCaseAndIdNot("CE 001 AA", vehicle.getId())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(vehicle.getId(),
                new VehiclesRequestDto("ce 001 aa", "Toyota", State.DISPOSABLE)));
        assertEquals("LT 452 AB", vehicle.getMatriculation());
    }

    @Test
    void update_UnknownVehicle_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(vehiclesRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(unknown, new VehiclesRequestDto("LT 1", "X", State.DISPOSABLE)));
    }

    // ─── get / list / delete ──────────────────────────────────────────────────

    @Test
    void get_DeletedVehicle_ShouldThrowResourceNotFound() {
        vehicle.markDeleted();
        when(vehiclesRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        assertThrows(ResourceNotFoundException.class, () -> service.get(vehicle.getId()));
    }

    @Test
    void list_ShouldReturnMappedPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(vehiclesRepository.findAllBy(pageable)).thenReturn(new PageImpl<>(List.of(vehicle), pageable, 1));

        ApiPageResponse<VehiclesResponseDto> page = service.list(pageable);

        assertEquals(1, page.content().size());
        assertEquals(vehicle.getId(), page.content().get(0).id());
    }

    @Test
    void delete_ShouldMarkVehicleAsDeleted() {
        when(vehiclesRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        service.delete(vehicle.getId());

        assertTrue(vehicle.isDeleted());
    }
}
