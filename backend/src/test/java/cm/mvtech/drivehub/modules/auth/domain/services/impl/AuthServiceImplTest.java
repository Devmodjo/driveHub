package cm.mvtech.drivehub.modules.auth.domain.services.impl;

import cm.mvtech.drivehub.modules.auth.application.dto.AuthResponse;
import cm.mvtech.drivehub.modules.auth.application.dto.CurrentUserResponse;
import cm.mvtech.drivehub.modules.auth.application.dto.ForgotPasswordRequest;
import cm.mvtech.drivehub.modules.auth.application.dto.LoginRequest;
import cm.mvtech.drivehub.modules.auth.application.dto.ResetPasswordRequest;
import cm.mvtech.drivehub.modules.auth.domain.model.EmailVerificationToken;
import cm.mvtech.drivehub.modules.auth.domain.model.PasswordResetToken;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.EmailVerificationTokenRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.PasswordResetTokenRepository;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.enums.Gender;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorRegisterRequest;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.infrastucture.repository.MonitorsRepository; // Utilisation de la typo 'infrastucture' du projet
import cm.mvtech.drivehub.modules.student.Student;
import cm.mvtech.drivehub.modules.student.StudentRegisterRequest;
import cm.mvtech.drivehub.modules.student.StudentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentsRepository studentsRepository;
    @Mock
    private MonitorsRepository monitorsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailVerificationTokenRepository emailTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private final String frontendUrl = "http://localhost:4200";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword");
        user.setRoles(Role.STUDENT);
        user.setProfileStatus(ProfileStatus.REGISTERED);
        user.setCreatedAt(LocalDate.now());

        // Inject the frontendUrl value into the authService
        ReflectionTestUtils.setField(authService, "frontendUrl", frontendUrl);
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("john.doe@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        AuthResponse response = authService.login(request);
        
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getRoles(), response.getRole());
        assertEquals(user.getProfileStatus(), response.getProfileStatus());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(eq("password"), anyString());
        verify(jwtService, times(1)).generateToken(any(User.class));
    }

    @Test
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));
        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_InvalidPassword() {
        LoginRequest request = new LoginRequest("john.doe@example.com", "wrongPassword");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> authService.login(request));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerStudent_Success() {
        StudentRegisterRequest request = new StudentRegisterRequest(
                "Jane", "Doe", "jane.doe@example.com", "password",
                "123456789", Gender.FEMALE, "Cameroonian", "Douala",new Date(2000, 1, 1)
        );
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.registerStudent(request);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(studentsRepository, times(1)).save(any(Student.class)); 
        verifyNoInteractions(emailTokenRepository, emailService);
    }

    @Test
    void registerStudent_EmailAlreadyUsed() {
        StudentRegisterRequest request = new StudentRegisterRequest(
                "Jane", "Doe", "john.doe@example.com", "password",
                "123456789", Gender.FEMALE, "Cameroonian", "Douala", new Date(2000, 1, 1)
        );
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.registerStudent(request));
        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoInteractions(passwordEncoder, studentsRepository, emailTokenRepository, emailService);
    }

    @Test
    void registerMonitor_Success() {
        MonitorRegisterRequest request = new MonitorRegisterRequest(
                "Mike", "Smith", "mike.smith@example.com", "password",
                "987654321", Gender.MALE, "Nigerian", "Lagos",new java.sql.Date(1990, 5, 10)
        );
        // Le repository est sollicité 2 fois : check existence ET sendVerificationEmail
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty()) 
                .thenReturn(Optional.of(user));
        
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        authService.registerMonitor(request);

        verify(userRepository, times(2)).findByEmail(anyString());
        verify(monitorsRepository, times(1)).save(any(Monitor.class));
        verify(emailTokenRepository, times(1)).save(any(EmailVerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerMonitor_EmailAlreadyUsed() {
        MonitorRegisterRequest request = new MonitorRegisterRequest(
                "Mike", "Smith", "john.doe@example.com", "password",
                "987654321", Gender.MALE, "Nigerian", "Lagos", new java.sql.Date(1990, 5, 10)
        );
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.registerMonitor(request));
        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoInteractions(passwordEncoder, monitorsRepository, emailTokenRepository, emailService);
    }

    @Test
    void getCurrentUser_Success() {
        // Correction du constructeur UserPrincipal (14 arguments requis)
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(), user.getFirstname(), user.getLastname(), 
                user.getEmail(), user.getPassword(), user.getRoles(), 
                user.getProfileStatus(), true, user.getCreatedAt(),
                true, true, true, true, 
                Collections.emptyList()
        );
        
        user.setFullProfile(true);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        CurrentUserResponse response = authService.getCurrentUser(authentication);

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getFirstname(), response.firstname());
        assertEquals(user.getEmail(), response.email());
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    void getCurrentUser_NotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> authService.getCurrentUser(authentication));
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(0)).getPrincipal();
    }

    @Test
    void getCurrentUser_UnsupportedPrincipal() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("unsupportedPrincipal");

        assertThrows(IllegalArgumentException.class, () -> authService.getCurrentUser(authentication));
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    void sendVerificationEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(emailTokenRepository.save(any(EmailVerificationToken.class))).thenReturn(new EmailVerificationToken());

        authService.sendVerificationEmail(user.getEmail());

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(emailTokenRepository, times(1)).deleteAllByUserId(any(UUID.class));
        verify(emailTokenRepository, times(1)).save(any(EmailVerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendVerificationEmail_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.sendVerificationEmail("nonexistent@example.com"));
        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoInteractions(emailTokenRepository, emailService);
    }

    @Test
    void verifyEmail_Success() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("validToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        when(emailTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(emailTokenRepository.save(any(EmailVerificationToken.class))).thenReturn(token);

        authService.verifyEmail("validToken");

        assertEquals(ProfileStatus.EMAIL_VERIFIED, user.getProfileStatus());
        assertTrue(token.isUsed());
        verify(emailTokenRepository, times(1)).findByToken(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailTokenRepository, times(1)).save(any(EmailVerificationToken.class));
    }

    @Test
    void verifyEmail_InvalidToken() {
        when(emailTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail("invalidToken"));
        verify(emailTokenRepository, times(1)).findByToken(anyString());
        verifyNoInteractions(userRepository);
    }

    @Test
    void verifyEmail_ExpiredToken() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("expiredToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().minusHours(1));
        token.setUsed(false);

        when(emailTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail("expiredToken"));
        verify(emailTokenRepository, times(1)).findByToken(anyString());
        verifyNoInteractions(userRepository);
    }

    @Test
    void verifyEmail_AlreadyUsedToken() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("usedToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(true);

        when(emailTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail("usedToken"));
        verify(emailTokenRepository, times(1)).findByToken(anyString());
        verifyNoInteractions(userRepository);
    }

    @Test
    void verifyEmail_AlreadyVerifiedUser() {
        user.setProfileStatus(ProfileStatus.EMAIL_VERIFIED);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("validToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        when(emailTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        authService.verifyEmail("validToken");

        verify(emailTokenRepository, times(1)).findByToken(anyString());
        verify(userRepository, times(0)).save(any(User.class)); // Should not save if already verified
        verify(emailTokenRepository, times(0)).save(any(EmailVerificationToken.class)); // Should not save token as used
    }

    @Test
    void forgotPassword_EmailExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john.doe@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(new PasswordResetToken());

        authService.forgotPassword(request);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordResetTokenRepository, times(1)).deleteAllByUserId(any(UUID.class));
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void forgotPassword_EmailDoesNotExist() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Should not throw an exception for anti-enumeration
        assertDoesNotThrow(() -> authService.forgotPassword(request));

        verify(userRepository, times(1)).findByEmail(anyString());
        verifyNoInteractions(passwordResetTokenRepository, emailService);
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest("validToken", "newPassword123");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("validToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(token);

        authService.resetPassword(request);

        assertEquals("newEncodedPassword", user.getPassword());
        assertTrue(token.isUsed());
        verify(passwordResetTokenRepository, times(1)).findByToken(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_InvalidToken() {
        ResetPasswordRequest request = new ResetPasswordRequest("invalidToken", "newPassword123");
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        verify(passwordResetTokenRepository, times(1)).findByToken(anyString());
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void resetPassword_ExpiredToken() {
        ResetPasswordRequest request = new ResetPasswordRequest("expiredToken", "newPassword123");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expiredToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().minusHours(1));
        token.setUsed(false);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        verify(passwordResetTokenRepository, times(1)).findByToken(anyString());
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void resetPassword_AlreadyUsedToken() {
        ResetPasswordRequest request = new ResetPasswordRequest("usedToken", "newPassword123");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("usedToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(true);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        verify(passwordResetTokenRepository, times(1)).findByToken(anyString());
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void resetPassword_WeakPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest("validToken", "short");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("validToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
        verify(passwordResetTokenRepository, times(1)).findByToken(anyString());
        verifyNoInteractions(passwordEncoder, userRepository);
    }
}
