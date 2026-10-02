package cm.mvtech.drivehub.modules.drivingschool.application.controller;

import cm.mvtech.drivehub.modules.auth.domain.model.UserPrincipal;
import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolRequestDto;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DrivingSchoolControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DrivingSchoolService drivingSchoolService;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper — crée un UserPrincipal de test
    private UserPrincipal buildMonitorPrincipal() {
        return new UserPrincipal(
                UUID.randomUUID(),
                "Alice", "Martin",
                "alice@test.cm",
                "encodedPassword",
                Role.MONITOR,
                ProfileStatus.ACTIVE,
                true,
                LocalDate.now(),
                true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_MONITOR"))
        );
    }

    private UsernamePasswordAuthenticationToken buildMonitorAuth() {
        UserPrincipal principal = buildMonitorPrincipal();
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
    }

    // ─── CREATE SCHOOL REQUEST ────────────────────────────────────────────────

    @Test
    void createSchoolRequest_AsMonitor_ShouldReturnCreated() throws Exception {
        // Le controller injecte @AuthenticationPrincipal UserPrincipal
        // donc on doit passer un vrai UserPrincipal dans le SecurityContext
        doNothing().when(drivingSchoolService).createSchool(any(), any());

        mockMvc.perform(post("/api/driving-schools/request")
                        .with(authentication(buildMonitorAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSchool("Meilleure école"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                // le moniteur est informé du délai de traitement
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        cm.mvtech.drivehub.modules.messageapi.ValidationDelay.TEXT)));
    }

    /** Demande valide, champs dans l'ordre du record DrivingSchoolRequestDto. */
    private static DrivingSchoolRequestDto validSchool(String description) {
        return new DrivingSchoolRequestDto(
                "Auto École Prestige",      // name
                "prestige@test.cm",         // email
                "Cameroun",                 // country
                "Yaoundé",                  // city
                "+237 699 00 00 00",        // phoneNumber
                "Rue 123, Bastos",          // address
                description,                // description
                "https://prestige.cm",      // websiteUrl
                "677000000"                 // whatsappNumber
        );
    }

    /**
     * Non-régression : une présentation de plus de 255 caractères faisait échouer l'insertion
     * (« value too long for type character varying(255) »). Elle est acceptée jusqu'à 2000 caractères.
     */
    @Test
    void createSchoolRequest_WithLongDescription_ShouldBeAccepted() throws Exception {
        doNothing().when(drivingSchoolService).createSchool(any(), any());

        mockMvc.perform(post("/api/driving-schools/request")
                        .with(authentication(buildMonitorAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSchool("a".repeat(1500)))))
                .andExpect(status().isCreated());
    }

    /** Au-delà de 2000 caractères : 400 avec un message précis sur le champ description. */
    @Test
    void createSchoolRequest_WithTooLongDescription_ShouldReturnFieldError() throws Exception {
        mockMvc.perform(post("/api/driving-schools/request")
                        .with(authentication(buildMonitorAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSchool("a".repeat(2001)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.description").value(
                        "La présentation ne doit pas dépasser 2000 caractères"));
    }

    @Test
    void createSchoolRequest_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Sans token → 401 (non authentifié)
        // Note: /api/driving-schools/** est en permitAll dans SecurityConfig
        // mais @PreAuthorize("hasRole('MONITOR')") bloque sans auth
        // Formulaire valide : ce test ne vérifie que les droits d'accès
        DrivingSchoolRequestDto dto = validSchool("Desc");

        mockMvc.perform(post("/api/driving-schools/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createSchoolRequest_AsStudent_ShouldBeForbidden() throws Exception {
        // @PreAuthorize("hasRole('MONITOR')") bloque les STUDENT
        // Formulaire valide : ce test ne vérifie que les droits d'accès
        DrivingSchoolRequestDto dto = validSchool("Desc");

        mockMvc.perform(post("/api/driving-schools/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // PUBLIC LIST

    @Test
    void retrievePublicSchools_WithoutAuth_ShouldReturnAccepted() throws Exception {
        when(drivingSchoolService.retreiveSchool()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/driving-schools/public/all"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void retrievePublicSchools_ShouldReturnEmptyList_WhenNoSchools() throws Exception {
        when(drivingSchoolService.retreiveSchool()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/driving-schools/public/all"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.length()").value(0));
    }
}