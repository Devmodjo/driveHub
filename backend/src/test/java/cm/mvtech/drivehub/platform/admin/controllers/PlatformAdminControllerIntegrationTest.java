package cm.mvtech.drivehub.platform.admin.controllers;

import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminAuthResponse;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminLoginRequest;
import cm.mvtech.drivehub.platform.admin.services.AdminerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminerService adminerService;

    @MockitoBean
    private DrivingSchoolService drivingSchoolService;

    @Autowired
    private ObjectMapper objectMapper;

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @Test
    void adminLogin_WithValidCredentials_ShouldReturnAccepted() throws Exception {
        PlatformAdminLoginRequest request =
                new PlatformAdminLoginRequest("admin@drivehub.cm", "adminPass");
        PlatformAdminAuthResponse response =
                new PlatformAdminAuthResponse("admin-token", AdminRole.ROOT, AdminStatus.ACTIVE);

        when(adminerService.adminerLogin(any())).thenReturn(response);

        mockMvc.perform(post("/api/platform/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.token").value("admin-token"))
                .andExpect(jsonPath("$.role").value("ROOT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void adminLogin_WithMissingPassword_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
                {
                    "email": "admin@drivehub.cm"
                }
                """;
        mockMvc.perform(post("/api/platform/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────────

    @Test
    void adminRegister_ShouldReturnCreated() throws Exception {
        // adminerRegistry retourne void → doNothing
        doNothing().when(adminerService).adminerRegistry(any());

        // JSON brut — correspond exactement aux champs de PlatformAdminCreateRequest :
        // name, email, role (AdminRole), password, residence, phoneNumber
        String registerJson = """
                {
                    "name": "Admin Test",
                    "email": "admin.test@drivehub.cm",
                    "role": "REVIEWER",
                    "password": "securePass123",
                    "residence": "Yaoundé",
                    "phoneNumber": "677000000"
                }
                """;

        mockMvc.perform(post("/api/platform/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Inscription réussie, en attente de validation par l'Administrateur ROOT"));
    }

    // ─── ACTIVATE ADMIN ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ROOT")
    void activateAdmin_AsRoot_ShouldReturnAccepted() throws Exception {
        UUID adminId = UUID.randomUUID();
        doNothing().when(adminerService).activateAdmin(any());

        mockMvc.perform(patch("/api/platform/admin/" + adminId + "/activate"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compte activé avec succès"));
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void activateAdmin_AsReviewer_ShouldBeForbidden() throws Exception {
        UUID adminId = UUID.randomUUID();

        mockMvc.perform(patch("/api/platform/admin/" + adminId + "/activate"))
                .andExpect(status().isForbidden());
    }

    @Test
    void activateAdmin_WithoutAuth_ShouldBeForbidden() throws Exception {
        UUID adminId = UUID.randomUUID();

        mockMvc.perform(patch("/api/platform/admin/" + adminId + "/activate"))
                .andExpect(status().isForbidden());
    }

    // ─── APPROVE REGISTRY ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "REVIEWER")
    void approveRegistry_AsReviewer_ShouldReturnAccepted() throws Exception {
        UUID registryId = UUID.randomUUID();
        doNothing().when(drivingSchoolService).approveRegistry(any());

        mockMvc.perform(patch("/api/platform/registries/" + registryId + "/approve"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("requête approuvé avec success"));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void approveRegistry_AsRoot_ShouldReturnAccepted() throws Exception {
        UUID registryId = UUID.randomUUID();
        doNothing().when(drivingSchoolService).approveRegistry(any());

        mockMvc.perform(patch("/api/platform/registries/" + registryId + "/approve"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void approveRegistry_AsSuperAdmin_ShouldBeForbidden() throws Exception {
        UUID registryId = UUID.randomUUID();

        mockMvc.perform(patch("/api/platform/registries/" + registryId + "/approve"))
                .andExpect(status().isForbidden());
    }

    // ─── PENDING ──────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "REVIEWER")
    void retrievePendingRegistries_AsReviewer_ShouldReturnOk() throws Exception {
        when(drivingSchoolService.retreivePendingRequest())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/platform/registries/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void retrievePendingAdmins_AsRoot_ShouldReturnAccepted() throws Exception {
        when(adminerService.pendingAdminerRequest())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/platform/admin/pending"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").isArray());
    }
}