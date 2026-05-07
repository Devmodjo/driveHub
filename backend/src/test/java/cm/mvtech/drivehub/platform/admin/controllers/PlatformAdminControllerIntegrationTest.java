package cm.mvtech.drivehub.platform.admin.controllers;

import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import cm.mvtech.drivehub.platform.admin.services.AdminerService;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminLoginRequest;
import cm.mvtech.drivehub.platform.admin.models.dto.PlatformAdminAuthResponse;
import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlatformAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminerService adminerService;

    @MockBean
    private DrivingSchoolService drivingSchoolService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminLogin_Success() throws Exception {
        PlatformAdminLoginRequest loginRequest = new PlatformAdminLoginRequest("admin@drivehub.cm", "adminPass");
        PlatformAdminAuthResponse authResponse = new PlatformAdminAuthResponse("admin-token", AdminRole.ROOT, AdminStatus.ACTIVE);

        when(adminerService.adminerLogin(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/platform/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonValue("$.adminToken").value("admin-token"));
    }

    @Test
    @WithMockUser(roles = "ROOT")
    void activateAdmin_AsRoot_ShouldWork() throws Exception {
        UUID adminId = UUID.randomUUID();
        doNothing().when(adminerService).activateAdmin(adminId);

        mockMvc.perform(patch("/api/platform/admin/" + adminId + "/activate"))
                .andExpect(status().isAccepted())
                .andExpect(jsonValue("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void approveRegistry_AsReviewer_ShouldWork() throws Exception {
        UUID registryId = UUID.randomUUID();
        doNothing().when(drivingSchoolService).approveRegistry(registryId);

        mockMvc.perform(patch("/api/platform/registries/" + registryId + "/approve"))
                .andExpect(status().isAccepted())
                .andExpect(jsonValue("$.message").value("requête approuvé avec success"));
    }
}