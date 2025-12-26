package cm.drivemaster.backend.services;

import cm.drivemaster.backend.models.dto.AuthResponse;
import cm.drivemaster.backend.models.dto.LoginRequest;
import cm.drivemaster.backend.models.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    void register(RegisterRequest request);
}

