package cm.drivemaster.backend.controllers;


import cm.drivemaster.backend.models.dto.*;
import cm.drivemaster.backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    private ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.login(loginRequest));
    }

    @PostMapping("/register")
    private ResponseEntity<ApiResponse> registerAdmin (@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Inscription réussie. En attente de validation."));
    }

    @PostMapping("/register/student")
    private ResponseEntity<ApiResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest registerRequest) {
        authService.registerStudent(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Inscription de l'étudiant réussie. En attente de validation par l'admin."));
    }

    @PostMapping("/register/monitor")
    private ResponseEntity<ApiResponse> registerMonitor(@Valid @RequestBody MonitorRegisterRequest registerRequest) {
        authService.registerMonitor(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Inscription de l'encadreur réussie. En attente de validation par l'admin."));
    }
}
