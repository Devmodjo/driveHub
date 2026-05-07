package cm.mvtech.drivehub.modules.drivingschool.application.controller;

import cm.mvtech.drivehub.modules.drivingschool.application.dto.DrivingSchoolRequestDto;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DrivingSchoolControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DrivingSchoolService drivingSchoolService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "MONITOR")
    void createSchoolRequest_AsMonitor_ShouldReturnCreated() throws Exception {
        DrivingSchoolRequestDto dto = new DrivingSchoolRequestDto(
                "New Auto School", "Rue 123", "699000000", "school@test.cm",
                "Best school", "www.school.cm", "677000000", "Cameroon", "Yaounde"
        );

        mockMvc.perform(post("/api/driving-schools/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonValue("$.success").value(true))
                .andExpect(jsonValue("$.message").value("Auto ecole enregistrée en attente de validation"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createSchoolRequest_AsStudent_ShouldBeForbidden() throws Exception {
        DrivingSchoolRequestDto dto = new DrivingSchoolRequestDto("Fail", "Addr", "123", "a@b.c", "D", "W", "W", "C", "C");

        mockMvc.perform(post("/api/driving-schools/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void retrievePublicSchools_ShouldWorkWithoutAuth() throws Exception {
        when(drivingSchoolService.retreiveSchool()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/driving-schools/public/all"))
                .andExpect(status().isAccepted());
    }
}