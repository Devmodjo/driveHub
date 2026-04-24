package cm.mvtech.drivehub.modules.auth.application.controller;


import cm.mvtech.drivehub.modules.auth.application.dto.CurrentUserResponse;
import cm.mvtech.drivehub.modules.auth.application.dto.ForgotPasswordRequest;
import cm.mvtech.drivehub.modules.auth.application.dto.ResetPasswordRequest;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import cm.mvtech.drivehub.modules.auth.application.dto.LoginRequest;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorRegisterRequest;
import cm.mvtech.drivehub.modules.student.StudentRegisterRequest;

import cm.mvtech.drivehub.modules.auth.domain.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.login(loginRequest));
    }

    @Operation(
            summary = "afficher l'utilisateur connecter",
            description = "apres authentification, ce enpoint permet d'afficher les information de l'utilisateur recemment connecter"
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("veuillez vous authentifié");
        }

        return ResponseEntity.status(200).body(authService.getCurrentUser(authentication));
    }

    @Operation(
            summary = "endpoint d'inscriptions des etudiant",
            description = "ici seule les utilisateurs avec le rôle STUDENT sont inscrit mais doivent être validé par un moniteur"
    )
    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest registerRequest) {
        authService.registerStudent(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Inscription de l'étudiant réussie. En attente de validation par le moniteur."));
    }

    @Operation(
            summary = "endpoint d'inscriptions des encadreur",
            description = "ici seule les utilisateurs avec le rôle MONITOR sont inscrit mais doivent être validé par un Admin de la plateforme. Ce pendant ils sont autorisé a ce connecter et à obtenir un JWT pour emettre une requete de creation d'une auto-école"
    )
    @PostMapping("/register/monitor")
    public ResponseEntity<ApiResponse> registerMonitor(@Valid @RequestBody MonitorRegisterRequest registerRequest) {
        authService.registerMonitor(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Inscription de l'encadreur réussie. En attente de validation par l'admin."));
    }

    @Operation(summary = "Vérification email",
            description = "Valide l'email via le token reçu par email")
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(
                new ApiResponse(true, "Email vérifié avec succès. Vous pouvez vous connecter."));
    }

    @Operation(summary = "Renvoyer l'email de vérification",
            description = "Renvoie un email de vérification si le précédent a expiré")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(
            @RequestParam String email) {
        authService.sendVerificationEmail(email);
        return ResponseEntity.ok(
                new ApiResponse(true, "Email de vérification renvoyé."));
    }

    @Operation(summary = "Mot de passe oublié",
            description = "Envoie un email de réinitialisation si le compte existe")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse(true,
                "Si un compte existe avec cet email, vous recevrez un lien de réinitialisation."));
    }

    @Operation(summary = "Réinitialisation du mot de passe",
            description = "Réinitialise le mot de passe via le token reçu par email")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                new ApiResponse(true, "Mot de passe réinitialisé avec succès."));
    }
}
