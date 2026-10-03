package cm.mvtech.drivehub.modules.auth.domain.model;

import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Non-régression : UserPrincipal.build() passait ses arguments dans le désordre
 * (fullProfile toujours true, enabled = fullProfile).
 */
class UserPrincipalTest {

    private static User user(Boolean fullProfile) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstname("Awa");
        user.setEmail("awa@test.cm");
        user.setPassword("hash");
        user.setRoles(Role.MONITOR);
        user.setProfileStatus(ProfileStatus.ACTIVE);
        user.setFullProfile(fullProfile);
        return user;
    }

    @Test
    void build_IncompleteProfile_IsStillEnabled() {
        UserPrincipal principal = UserPrincipal.build(user(false));

        assertFalse(principal.getFullProfile());
        assertTrue(principal.isEnabled(), "un profil incomplet ne doit pas désactiver le compte");
        assertTrue(principal.isAccountNonLocked());
        assertEquals("ROLE_MONITOR", principal.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void build_CompleteProfile_KeepsFullProfile() {
        assertTrue(UserPrincipal.build(user(true)).getFullProfile());
    }

    @Test
    void build_NullFullProfile_IsTreatedAsIncomplete() {
        assertFalse(UserPrincipal.build(user(null)).getFullProfile());
    }
}
