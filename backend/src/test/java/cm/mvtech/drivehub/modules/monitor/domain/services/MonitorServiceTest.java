package cm.mvtech.drivehub.modules.monitor.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.infrastructure.mapper.MonitorMapper;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link MonitorService} : liste des moniteurs du tenant, profil du
 * moniteur connecté et recherche par identifiant.
 */
@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    @Mock private MonitorsRepository monitorsRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Spy  private MonitorMapper mapper = Mappers.getMapper(MonitorMapper.class);

    @InjectMocks
    private MonitorService service;

    private User monitorUser;
    private Monitor monitor;

    @BeforeEach
    void setUp() {
        monitorUser = TestData.user(Role.MONITOR);
        monitor = TestData.monitor(monitorUser);
    }

    /** Les moniteurs supprimés logiquement ne doivent pas apparaître dans la liste. */
    @Test
    void list_ShouldExcludeDeletedMonitors() {
        Monitor deleted = TestData.monitor(TestData.user(Role.MONITOR));
        deleted.markDeleted();
        when(monitorsRepository.findAllByOrderByCreatedOnAsc()).thenReturn(List.of(monitor, deleted));

        List<MonitorResponseDto> result = service.list();

        assertEquals(1, result.size());
        assertEquals(monitor.getId(), result.get(0).id());
        assertEquals(monitorUser.getEmail(), result.get(0).email());
    }

    @Test
    void me_ShouldReturnProfileOfConnectedMonitor() {
        when(currentUserProvider.get()).thenReturn(TestData.principal(monitorUser));
        when(monitorsRepository.findFirstByUser_Id(monitorUser.getId())).thenReturn(Optional.of(monitor));

        MonitorResponseDto dto = service.me();

        assertEquals(monitor.getId(), dto.id());
        assertEquals(monitorUser.getId(), dto.userId());
    }

    @Test
    void me_WithoutMonitorProfile_ShouldThrowResourceNotFound() {
        when(currentUserProvider.get()).thenReturn(TestData.principal(monitorUser));
        when(monitorsRepository.findFirstByUser_Id(monitorUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.me());
    }

    @Test
    void getEntity_ExistingMonitor_ShouldReturnIt() {
        when(monitorsRepository.findById(monitor.getId())).thenReturn(Optional.of(monitor));

        assertSame(monitor, service.getEntity(monitor.getId()));
    }

    @Test
    void getEntity_UnknownOrDeletedMonitor_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(monitorsRepository.findById(unknown)).thenReturn(Optional.empty());
        monitor.markDeleted();
        when(monitorsRepository.findById(monitor.getId())).thenReturn(Optional.of(monitor));

        assertThrows(ResourceNotFoundException.class, () -> service.getEntity(unknown));
        assertThrows(ResourceNotFoundException.class, () -> service.getEntity(monitor.getId()));
    }
}
