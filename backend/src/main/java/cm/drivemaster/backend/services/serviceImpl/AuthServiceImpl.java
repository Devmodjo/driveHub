package cm.drivemaster.backend.services.serviceImpl;


import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.configs.PasswordEncoderConfig;
import cm.drivemaster.backend.core.TenantContext;
import cm.drivemaster.backend.enums.ProfileStatus;
import cm.drivemaster.backend.enums.Role;
import cm.drivemaster.backend.models.dto.AuthResponse;
import cm.drivemaster.backend.models.dto.LoginRequest;
import cm.drivemaster.backend.models.dto.RegisterRequest;
import cm.drivemaster.backend.repositories.UserRepository;
import cm.drivemaster.backend.services.AuthService;
import cm.drivemaster.backend.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {

        // authentification spring security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // récupération de l'utilisateur dans le schéma courant
        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new UsernameNotFoundException("Utilisateur introuvable")
        );

        // génération du token
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getRoles(),
                user.getProfileStatus(),
                user.getFullProfile()
        );
    }

    @Override
    public void register(RegisterRequest request) {

        // Vérification email unique
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        // Création utilisateur technique
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(request.role());

        // État INITIAL (PAS métier)
        user.setProfileStatus(ProfileStatus.REGISTERED);
        user.setFullProfile(false);

        userRepository.save(user);
    }

}
