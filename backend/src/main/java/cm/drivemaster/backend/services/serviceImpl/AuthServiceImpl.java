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


    @Override
    public AuthResponse login(LoginRequest request) {

        // Charger l'utilisateur
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        // Vérifier le mot de passe manuellement
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AccessDeniedException("Email ou mot de passe incorrect");
        }

        // Chargement utilisateur (BON SCHÉMA)
        user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur introuvable"));


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
    public void registerStudent(StudentRegisterRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email déja utilisé");
        }

        User user = new User();
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setRoles(Role.STUDENT);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProfileStatus(ProfileStatus.REGISTERED);

        userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setPhoneNumber(request.phoneNumber());
        student.setGender(request.gender());
        student.setNationality(request.nationality());
        student.setResidenceCity(request.residenceCity());
        student.setDateOfBirth(request.dateOfBirth());


        studentsRepository.save(student);

    }

    @Override
    public void registerMonitor(MonitorRegisterRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email déja utilisé");
        }

        User user = new User();
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setRoles(Role.MONITOR);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProfileStatus(ProfileStatus.REGISTERED);

        userRepository.save(user);

        Monitor monitor = new Monitor();
        monitor.setUser(user);
        monitor.setPhoneNumber(request.phoneNumber());
        monitor.setGender(request.gender());
        monitor.setNationality(request.nationality());
        monitor.setResidenceCity(request.residenceCity());
        monitor.setDateOfBirth(request.dateOfBirth());
        monitorsRepository.save(monitor);
    }

}
