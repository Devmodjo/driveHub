package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.application.dto.*;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorRegisterRequest;
import cm.mvtech.drivehub.modules.student.StudentRegisterRequest;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    void registerStudent(StudentRegisterRequest request);

    void registerMonitor(MonitorRegisterRequest request);

    CurrentUserResponse getCurrentUser(Authentication authentication);

    void sendVerificationEmail(String email);
    void verifyEmail(String token);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}

