package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.SchoolJoinRequest;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.SchoolJoinRequestRepository;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link UserTenantResolver} : le serveur détermine lui-même l'auto-école
 * (schéma) d'un utilisateur, valeur ensuite inscrite dans le JWT (claim {@code tenant}).
 */
@ExtendWith(MockitoExtension.class)
class UserTenantResolverTest {

    @Mock private DrivingSchoolRegistryRepository registryRepository;
    @Mock private SchoolJoinRequestRepository joinRequestRepository;

    @InjectMocks
    private UserTenantResolver resolver;

    /** Moniteur fondateur d'une auto-école validée : son tenant est le schéma de cette auto-école. */
    @Test
    void owner_OfApprovedSchool_ShouldGetItsSchema() {
        User owner = TestData.user(Role.MONITOR);
        DrivingSchoolRegistry registry = TestData.registry(owner, DrivingSchoolStatus.APPROVED);
        when(registryRepository.findByAdmin(owner)).thenReturn(Optional.of(registry));

        assertEquals(Optional.of(registry.getSchemaName()), resolver.resolveTenant(owner));
        verifyNoInteractions(joinRequestRepository);
    }

    /** Demande de création encore en attente : pas de tenant (on cherche alors une adhésion). */
    @Test
    void owner_OfPendingSchool_ShouldGetNoTenant() {
        User owner = TestData.user(Role.MONITOR);
        when(registryRepository.findByAdmin(owner))
                .thenReturn(Optional.of(TestData.registry(owner, DrivingSchoolStatus.PENDING)));
        when(joinRequestRepository.findFirstByUserAndJoinStatusOrderByCreatedOnDesc(owner, JoinStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertTrue(resolver.resolveTenant(owner).isEmpty());
    }

    /** Élève dont l'adhésion a été acceptée : tenant = schéma de l'auto-école rejointe. */
    @Test
    void member_WithApprovedJoinRequest_ShouldGetSchoolSchema() {
        User student = TestData.user(Role.STUDENT);
        DrivingSchoolRegistry registry = TestData.registry(TestData.user(Role.MONITOR), DrivingSchoolStatus.ACTIVE);
        SchoolJoinRequest accepted = new SchoolJoinRequest(student, registry.getId(), Role.STUDENT, JoinStatus.APPROVED);
        when(registryRepository.findByAdmin(student)).thenReturn(Optional.empty());
        when(joinRequestRepository.findFirstByUserAndJoinStatusOrderByCreatedOnDesc(student, JoinStatus.APPROVED))
                .thenReturn(Optional.of(accepted));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));

        assertEquals(Optional.of(registry.getSchemaName()), resolver.resolveTenant(student));
    }

    /** Auto-école suspendue : l'élève perd l'accès (aucun tenant dans le jeton). */
    @Test
    void member_OfSuspendedSchool_ShouldGetNoTenant() {
        User student = TestData.user(Role.STUDENT);
        DrivingSchoolRegistry registry = TestData.registry(TestData.user(Role.MONITOR), DrivingSchoolStatus.SUSPENDED);
        SchoolJoinRequest accepted = new SchoolJoinRequest(student, registry.getId(), Role.STUDENT, JoinStatus.APPROVED);
        when(registryRepository.findByAdmin(student)).thenReturn(Optional.empty());
        when(joinRequestRepository.findFirstByUserAndJoinStatusOrderByCreatedOnDesc(student, JoinStatus.APPROVED))
                .thenReturn(Optional.of(accepted));
        when(registryRepository.findById(registry.getId())).thenReturn(Optional.of(registry));

        assertTrue(resolver.resolveTenant(student).isEmpty());
    }

    /** Ni fondateur ni membre : pas de tenant. */
    @Test
    void userWithoutSchool_ShouldGetNoTenant() {
        User student = TestData.user(Role.STUDENT);
        when(registryRepository.findByAdmin(student)).thenReturn(Optional.empty());
        when(joinRequestRepository.findFirstByUserAndJoinStatusOrderByCreatedOnDesc(student, JoinStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertTrue(resolver.resolveTenant(student).isEmpty());
    }
}
