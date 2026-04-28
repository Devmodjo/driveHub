package cm.mvtech.drivehub.modules.auth.domain.services.impl;


import cm.mvtech.drivehub.modules.auth.application.dto.CurrentUserResponse;
import cm.mvtech.drivehub.modules.auth.application.dto.ForgotPasswordRequest;
import cm.mvtech.drivehub.modules.auth.application.dto.ResetPasswordRequest;
import cm.mvtech.drivehub.modules.auth.domain.model.EmailVerificationToken;
import cm.mvtech.drivehub.modules.auth.domain.model.PasswordResetToken;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.EmailVerificationTokenRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
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

    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;


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
       /** user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur introuvable"));
        */
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
        user.setProfileStatus(ProfileStatus.ACTIVE);

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

        sendVerificationEmail(user.getEmail());
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

    @Override
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        // Supprimer les anciens tokens
        emailTokenRepository.deleteAllByUserId(user.getId());

        // Créer le nouveau token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationToken.setUsed(false);
        emailTokenRepository.save(verificationToken);

        // Envoyer l'email (async)
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFirstname(),
                verificationUrl
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (verificationToken.isExpired()) {
            throw new IllegalArgumentException("Token expiré — demandez un nouveau lien");
        }

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Token déjà utilisé");
        }

        User user = verificationToken.getUser();

        // Déjà vérifié → succès silencieux
        if (user.getProfileStatus() == ProfileStatus.EMAIL_VERIFIED
                || user.getProfileStatus() == ProfileStatus.ACTIVE) {
            return;
        }

        // Activer le compte
        user.setProfileStatus(ProfileStatus.EMAIL_VERIFIED);
        userRepository.save(user);

        // Invalider le token
        verificationToken.setUsed(true);
        emailTokenRepository.save(verificationToken);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Comportement identique que l'email existe ou non (anti-énumération)
        userRepository.findByEmail(request.email()).ifPresent(user -> {

            // Supprimer les anciens tokens
            passwordResetTokenRepository.deleteAllByUserId(user.getId());

            // Créer le token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(token);
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            // Envoyer l'email (async)
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFirstname(),
                    resetUrl
            );
        });
        // Pas d'exception si email inconnu → anti-énumération
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Token expiré — refaites la demande");
        }

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token déjà utilisé");
        }

        if (request.newPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Invalider le token
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }


}
