package cm.drivemaster.backend.services;

import cm.drivemaster.backend.models.dto.*;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void registerStudent(StudentRegisterRequest request);

    void registerMonitor(MonitorRegisterRequest request);
}

