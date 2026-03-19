package cm.drivemaster.backend.beans;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {


    private final long id;
    private final String firstname;
    private final String lastname;
    private final String email;
    private final String password;

    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * method de build Factory, pour convervirt un User en UserPrincipal
     * @param user authentificated
     * @return Userprincipal
     */
    public static UserPrincipal build(User user) {

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRoles().toString().toUpperCase());
        List<GrantedAuthority> grantedAuthorities = List.of(authority);

        return new UserPrincipal(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                grantedAuthorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
