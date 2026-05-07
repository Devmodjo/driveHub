package cm.mvtech.drivehub.modules.auth.application.controller;

import cm.mvtech.drivehub.modules.auth.application.dto.*;
import cm.mvtech.drivehub.modules.auth.domain.services.AuthService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ShouldReturnAccepted() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        AuthResponse authResponse = new AuthResponse(UUID.randomUUID(), "mock-jwt-token", Role.STUDENT, ProfileStatus.ACTIVE, true);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonValue("$.token").value("mock-jwt-token"))
                .andExpect(jsonValue("$.role").value("STUDENT"));
    }

    @Test
    void registerStudent_ShouldReturnCreated() throws Exception {
        // Note: Utilisation de chaînes pour simplifier le test d'intégration
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
                .andExpect(jsonValue("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "STUDENT")
    void me_ShouldReturnCurrentUser() throws Exception {
        CurrentUserResponse userResponse = new CurrentUserResponse(
                UUID.randomUUID(), "John", "Doe", "user@test.com", 
                Role.STUDENT, ProfileStatus.ACTIVE, LocalDate.now(), true
        );

        when(authService.getCurrentUser(any())).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonValue("$.email").value("user@test.com"))
                .andExpect(jsonValue("$.firstname").value("John"));
    }

    @Test
    void me_WhenNotAuthenticated_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void forgotPassword_ShouldReturnOk() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonValue("$.success").value(true));
    }
}