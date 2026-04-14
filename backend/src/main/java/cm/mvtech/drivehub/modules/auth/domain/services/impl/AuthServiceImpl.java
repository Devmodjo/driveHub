package cm.mvtech.drivehub.modules.auth.domain.services.impl;


import cm.mvtech.drivehub.modules.auth.application.dto.CurrentUserResponse;
import cm.mvtech.drivehub.modules.auth.application.dto.UserResponseDto;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.auth.domain.services.AuthService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.student.Student;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.auth.application.dto.AuthResponse;
import cm.mvtech.drivehub.modules.auth.application.dto.LoginRequest;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorRegisterRequest;
import cm.mvtech.drivehub.modules.student.StudentRegisterRequest;

import cm.mvtech.drivehub.modules.monitor.infrastucture.repository.MonitorsRepository;
import cm.mvtech.drivehub.modules.student.StudentsRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;


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
                user.getId(),
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

    @Override
    public CurrentUserResponse getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Ce endpoint nécessite une authentification");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal user) {

            UUID id = user.getId();
            String firstname = user.getFirstname();
            String lastname = user.getLastname();
            String email = user.getEmail();
            Role role = user.getRole();
            ProfileStatus profileStatus = user.getProfileStatus();
            LocalDate createdAt = user.getCreatedAt();
            Boolean fullprofile = user.getFullProfile();

            return  new CurrentUserResponse(id, firstname, lastname, email, role, profileStatus, createdAt, fullprofile);
        }

        throw new IllegalArgumentException("Type de principal non supporté : " +
                principal.getClass().getName());
    }

}
