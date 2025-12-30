package cm.drivemaster.backend.services.serviceImpl;


import cm.drivemaster.backend.beans.Monitor;
import cm.drivemaster.backend.beans.Student;
import cm.drivemaster.backend.beans.User;
import cm.drivemaster.backend.enums.ProfileStatus;
import cm.drivemaster.backend.enums.Role;
import cm.drivemaster.backend.models.dto.*;
import cm.drivemaster.backend.repositories.MonitorsRepository;
import cm.drivemaster.backend.repositories.StudentsRepository;
import cm.drivemaster.backend.repositories.UserRepository;
import cm.drivemaster.backend.services.AuthService;
import cm.drivemaster.backend.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final StudentsRepository studentsRepository;
    private final MonitorsRepository monitorsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {

        // Authentification Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Chargement utilisateur (BON SCHÉMA)
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur introuvable"));

        // Vérification métier
//        if (user.getProfileStatus() != ProfileStatus.ACTIVE) {
//            throw new AccessDeniedException("Compte non activé");
//        }

        // Génération JWT tenant-aware
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

    @Override
    public void registerStudent(StudentRegisterRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email déja utilisé");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRoles(Role.STUDENT);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProfileStatus(ProfileStatus.REGISTERED);

        userRepository.save(user);

        Student student = new Student();
        student.setUser(user);

        studentsRepository.save(student);

    }

    @Override
    public void registerMonitor(MonitorRegisterRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email déja utilisé");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRoles(Role.MONITOR);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProfileStatus(ProfileStatus.REGISTERED);

        userRepository.save(user);

        Monitor monitor = new Monitor();
        monitor.setUser(user);
        monitor.setPhoneNumber(request.phoneNumber());

        monitorsRepository.save(monitor);
    }

}
