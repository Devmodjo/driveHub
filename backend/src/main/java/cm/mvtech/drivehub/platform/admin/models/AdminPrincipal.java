package cm.mvtech.drivehub.platform.admin.models;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Getter
@RequiredArgsConstructor
public class AdminPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final AdminRole role;
    private final AdminStatus status;
    private final LocalDateTime createdAt;

    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final Collection<? extends GrantedAuthority> authorities;


    public static AdminPrincipal build(PlatformAdmin admin) {

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + admin.getRole().toString().toUpperCase());
        List<GrantedAuthority> grantedAuthorities = List.of(authority);

        return new AdminPrincipal(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getPassword(),
                admin.getRole(),
                admin.getAdminStatus(),
                admin.getCreatedAt(),
                true,
                true,
                true,
                true,
                grantedAuthorities
        );
    }

}
