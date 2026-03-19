package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**     boolean enabled =
//                user.getProfileStatus() == ProfileStatus.REGISTERED
//                        || user.getProfileStatus() == ProfileStatus.ACTIVE;

     * IMPORTANT :
     * - Cette méthode est appelée APRÈS TenantResolutionFilter
     * - Le schéma PostgreSQL est donc DÉJÀ positionné
     * - On ne résout PAS le tenant ici
     */
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Utilisateur introuvable avec l'email : " + email
                        )
                );

        /*
         * RÈGLE D’AUTHENTIFICATION
         *
         * - REGISTERED  → peut se connecter (auth partielle)
         * - ACTIVE      → peut se connecter
         * - SUSPENDED / BLOCKED / INACTIVE → refus
         *
         * Le contrôle métier (fullProfile, etc.)
         *    se fait PLUS TARD, dans les services
         */
        return UserPrincipal.build(user);

//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(),                 // username
//                user.getPassword(),              // password (déjà encodé)
//                true,                          // enabled
//                true,                             // accountNonExpired
//                true,                             // credentialsNonExpired
//                true,                             // accountNonLocked
//                Collections.singleton(
//                        new SimpleGrantedAuthority(
//                                "ROLE_" + user.getRoles().name()
//                        )
//                )
//        );
    }
}