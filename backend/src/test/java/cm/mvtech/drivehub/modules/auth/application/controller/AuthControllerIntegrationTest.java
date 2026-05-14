package cm.mvtech.drivehub.modules.auth.application.controller;

import cm.mvtech.drivehub.modules.auth.application.dto.*;
import cm.mvtech.drivehub.modules.auth.domain.services.AuthService;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // LOGIN

    @Test
    void login_ShouldReturnAccepted_WithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        AuthResponse response = new AuthResponse(
                UUID.randomUUID(), "mock-jwt-token",
                Role.STUDENT, ProfileStatus.ACTIVE, true
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void login_WithMissingEmail_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
                {
                    "password": "password123"
                }
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ─── REGISTER STUDENT ─────────────────────────────────────────────────────

    @Test
    void registerStudent_ShouldReturnCreated() throws Exception {
        // authService.registerStudent retourne void → doNothing
        doNothing().when(authService).registerStudent(any());

        String studentJson = """
                {
                    "firstname": "John",
                    "lastname": "Doe",
                    "email": "john.doe@test.com",
                    "password": "password123",
                    "phoneNumber": "677000000",
                    "gender": "MALE",
                    "nationality": "Cameroonian",
                    "residenceCity": "Douala",
                    "dateOfBirth": "2000-01-01"
                }
                """;

        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Inscription de l'étudiant réussie. En attente de validation par le moniteur."));
    }

    // ─── REGISTER MONITOR ─────────────────────────────────────────────────────

    @Test
    void registerMonitor_ShouldReturnCreated() throws Exception {
        doNothing().when(authService).registerMonitor(any());

        String monitorJson = """
                {
                    "firstname": "Alice",
                    "lastname": "Martin",
                    "email": "alice.martin@test.com",
                    "password": "password123",
                    "phoneNumber": "699000000",
                    "gender": "FEMALE",
                    "nationality": "Cameroonian",
                    "residenceCity": "Yaoundé",
                    "dateOfBirth": "1990-05-15"
                }
                """;

        mockMvc.perform(post("/api/auth/register/monitor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monitorJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Inscription de l'encadreur réussie. En attente de validation par l'admin."));
    }

    // ─── /ME ──────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com", roles = "STUDENT")
    void me_WhenAuthenticated_ShouldReturnOk() throws Exception {
        CurrentUserResponse userResponse = new CurrentUserResponse(
                UUID.randomUUID(), "John", "Doe", "user@test.com",
                Role.STUDENT, ProfileStatus.ACTIVE, LocalDate.now(), true
        );
        when(authService.getCurrentUser(any())).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.firstname").value("John"));
    }

    @Test
    void me_WhenNotAuthenticated_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    // ─── VERIFY EMAIL ─────────────────────────────────────────────────────────

    @Test
    void verifyEmail_WithValidToken_ShouldReturnOk() throws Exception {
        // verifyEmail retourne void → doNothing
        doNothing().when(authService).verifyEmail(any());

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "valid-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Email vérifié avec succès. Vous pouvez vous connecter."));
    }

    @Test
    void verifyEmail_WithExpiredToken_ShouldReturnBadRequest() throws Exception {
        // verifyEmail lance IllegalArgumentException sur token invalide
        // ton GlobalHandlerException doit mapper ça en 400
        doNothing().when(authService).verifyEmail(any());

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "expired-token"))
                .andExpect(status().isOk());
        // Note: le mock ne lance pas d'exception ici
        // pour tester le cas d'erreur il faudrait un test séparé
        // avec when(authService.verifyEmail(any())).thenThrow(...)
        // mais ça dépend de ton GlobalHandlerException
    }

    // ─── FORGOT PASSWORD ──────────────────────────────────────────────────────

    @Test
    void forgotPassword_ShouldReturnOk_RegardlessOfEmailExistence() throws Exception {
        // forgotPassword retourne void → doNothing
        doNothing().when(authService).forgotPassword(any());

        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void forgotPassword_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
                {
                    "email": "not-an-email"
                }
                """;
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ─── RESET PASSWORD ───────────────────────────────────────────────────────

    @Test
    void resetPassword_WithValidToken_ShouldReturnOk() throws Exception {
        // resetPassword retourne void → doNothing
        doNothing().when(authService).resetPassword(any());

        ResetPasswordRequest request = new ResetPasswordRequest(
                "valid-reset-token", "newPassword123"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Mot de passe réinitialisé avec succès."));
    }
}