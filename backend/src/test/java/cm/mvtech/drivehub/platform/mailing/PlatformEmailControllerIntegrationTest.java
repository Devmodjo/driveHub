package cm.mvtech.drivehub.platform.mailing;

import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de l'API d'envoi d'emails du back-office (/api/platform/emails) :
 * vraie base PostgreSQL, sécurité et validation réelles. Seul EmailService est simulé
 * (aucun email ne part pendant les tests).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlatformEmailControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    /** ROOT envoie à deux adresses choisies : 202, envoi en arrière-plan, puis visible dans l'historique. */
    @Test
    @WithMockUser(username = "root@drivehub.cm", roles = "ROOT")
    void send_IndividualAsRoot_ShouldBeAcceptedAndAppearInHistory() throws Exception {
        String subject = "Test intégration " + UUID.randomUUID().toString().substring(0, 8);
        String body = """
                {
                  "audience": "INDIVIDUAL",
                  "recipients": ["Awa@Test.cm", "jean@test.cm", "awa@test.cm"],
                  "subject": "%s",
                  "message": "Bonjour,\\nCeci est un message de test."
                }
                """.formatted(subject);

        mockMvc.perform(post("/api/platform/emails").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.recipientCount").value(2))   // doublon "Awa@Test.cm" / "awa@test.cm" supprimé
                .andExpect(jsonPath("$.id").exists());

        verify(emailService).sendPlatformAnnouncement(anyList(), eq(subject), eq("Bonjour,\nCeci est un message de test."));

        mockMvc.perform(get("/api/platform/emails").param("page", "0").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.subject == '%s')].sentByEmail".formatted(subject)).value("root@drivehub.cm"));
    }

    /** Un REVIEWER ne peut pas envoyer d'emails. */
    @Test
    @WithMockUser(roles = "REVIEWER")
    void send_AsReviewer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(post("/api/platform/emails").contentType(MediaType.APPLICATION_JSON).content("""
                        { "audience": "ALL_MONITORS", "subject": "Objet", "message": "Un message assez long" }
                        """))
                .andExpect(status().isForbidden());
        verifyNoInteractions(emailService);
    }

    /** Sans jeton : 401. */
    @Test
    void send_WithoutAuthentication_ShouldBeUnauthorized() throws Exception {
        mockMvc.perform(post("/api/platform/emails").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    /** Objet trop court et message vide : un message d'erreur par champ. */
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void send_WithInvalidFields_ShouldReturnFieldErrors() throws Exception {
        mockMvc.perform(post("/api/platform/emails").contentType(MediaType.APPLICATION_JSON).content("""
                        { "audience": "ALL_STUDENTS", "subject": "A", "message": "" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.subject").value("L'objet doit contenir entre 3 et 150 caractères"))
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    /** « Membres d'une auto-école » sans auto-école choisie : message explicite. */
    @Test
    @WithMockUser(roles = "ROOT")
    void send_SchoolMembersWithoutSchool_ShouldExplainWhatIsMissing() throws Exception {
        mockMvc.perform(post("/api/platform/emails").contentType(MediaType.APPLICATION_JSON).content("""
                        { "audience": "SCHOOL_MEMBERS", "subject": "Objet", "message": "Un message assez long" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Choisissez l'auto-école dont les membres recevront l'email"));
    }

    /** Aperçu du nombre de destinataires et recherche (réponses bien formées sur une vraie base). */
    @Test
    @WithMockUser(roles = "ROOT")
    void audienceCountAndSearch_ShouldAnswer() throws Exception {
        mockMvc.perform(get("/api/platform/emails/audience-count").param("audience", "ALL_STUDENTS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientCount").isNumber());

        mockMvc.perform(get("/api/platform/emails/recipients").param("q", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());   // moins de 2 caractères : aucune recherche
    }

    /** Valeur d'audience inconnue : 400 qui nomme le paramètre. */
    @Test
    @WithMockUser(roles = "ROOT")
    void audienceCount_WithUnknownAudience_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/platform/emails/audience-count").param("audience", "TOUT_LE_MONDE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valeur invalide pour le paramètre « audience »"));
    }
}
