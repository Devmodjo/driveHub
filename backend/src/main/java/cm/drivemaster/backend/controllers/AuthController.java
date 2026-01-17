package cm.drivemaster.backend.controllers;


import cm.drivemaster.backend.models.dto.*;
import cm.drivemaster.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
@Tag(name = "USER API", description = "api d'authentification des utilisateurs lambda de la plateforme")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "login des users",
            description = "endpoint d'authentification des utilisateur en fonction de leurs roles"
    )
    @PostMapping("/login")
    private ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.login(loginRequest));
    }

    @Operation(
            summary = "endpoint d'inscriptions des etudiant",
            description = "ici seule les utilisateurs avec le rôle STUDENT sont inscrit mais doivent être validé par un moniteur"
    )
    @PostMapping("/register/student")
    private ResponseEntity<ApiResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest registerRequest) {
        authService.registerStudent(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Inscription de l'étudiant réussie. En attente de validation par le moniteur."));
    }

    @Operation(
            summary = "endpoint d'inscriptions des encadreur",
            description = "ici seule les utilisateurs avec le rôle MONITOR sont inscrit mais doivent être validé par un Admin de la plateforme"
    )
    @PostMapping("/register/monitor")
    private ResponseEntity<ApiResponse> registerMonitor(@Valid @RequestBody MonitorRegisterRequest registerRequest) {
        authService.registerMonitor(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Inscription de l'encadreur réussie. En attente de validation par l'admin."));
    }
}
