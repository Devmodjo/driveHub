package cm.drivemaster.backend.services;

import cm.drivemaster.backend.models.dto.AuthResponse;
import cm.drivemaster.backend.models.dto.LoginRequest;
import cm.drivemaster.backend.models.dto.MonitorRegisterRequest;
import cm.drivemaster.backend.models.dto.StudentRegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    void registerStudent(StudentRegisterRequest request);

    void registerMonitor(MonitorRegisterRequest request);
}

