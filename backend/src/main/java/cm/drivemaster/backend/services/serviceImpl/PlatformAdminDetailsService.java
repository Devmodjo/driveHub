package cm.drivemaster.backend.services.serviceImpl;

import cm.drivemaster.backend.beans.PlatformAdmin;
import cm.drivemaster.backend.enums.AdminStatus;
import cm.drivemaster.backend.repositories.PlatformAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformAdminDetailsService
        implements UserDetailsService {

    private final PlatformAdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        PlatformAdmin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin introuvable"));

        if (admin.getAdminStatus() != AdminStatus.ACTIVE) {
            throw new DisabledException("Compte admin en attente");
        }

        return new org.springframework.security.core.userdetails.User(
                admin.getEmail(),
                admin.getPassword(),
                admin.isEnabled(),
                true,
                true,
                true,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + admin.getRole().name()
                        )
                )
        );
    }
}
