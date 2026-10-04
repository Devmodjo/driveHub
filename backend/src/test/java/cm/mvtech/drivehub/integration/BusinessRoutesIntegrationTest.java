package cm.mvtech.drivehub.integration;

import cm.mvtech.drivehub.core.infrastructure.TenantSchemas;
import cm.mvtech.drivehub.modules.auth.domain.model.User;
import cm.mvtech.drivehub.modules.auth.domain.services.EmailService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.modules.auth.domain.services.UserTenantResolver;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchoolRegistry;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.DrivingSchoolService;
import cm.mvtech.drivehub.modules.drivingschool.infrastructure.DrivingSchoolRegistryRepository;
import cm.mvtech.drivehub.modules.enums.DrivingSchoolStatus;
import cm.mvtech.drivehub.modules.enums.Gender;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.infrastructure.repository.MonitorsRepository;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentSummaryDto;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.infrastructure.repository.StudentsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration des routes MÉTIER, avec une vraie base PostgreSQL et un vrai schéma tenant.
 *
 * <h2>Mise en place (une seule fois pour toute la classe, voir {@link #createTenantWithMonitorAndStudent()})</h2>
 * <ol>
 *   <li>un moniteur (compte + profil public) et une demande d'auto-école sont créés en base ;</li>
 *   <li>la demande est approuvée par le VRAI service ({@code DrivingSchoolService.approveRegistry}) :
 *       le schéma PostgreSQL de l'auto-école est créé par Flyway ;</li>
 *   <li>un élève demande à rejoindre l'auto-école puis le moniteur accepte, via les VRAIES routes HTTP ;</li>
 *   <li>les jetons JWT sont produits par {@link JwtService} avec le tenant calculé par le serveur
 *       ({@link UserTenantResolver}), exactement comme lors d'une connexion.</li>
 * </ol>
 *
 * <h2>Rejouabilité</h2>
 * <p>Chaque exécution utilise un suffixe aléatoire (emails, nom d'auto-école, schéma) : la classe
 * peut être relancée autant de fois que voulu sur la même base. Les données créées sont supprimées
 * à la fin ({@link #cleanUp()}).</p>
 *
 * <p>{@link EmailService} est remplacé par un mock : aucun email n'est réellement envoyé.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)   // permet un @BeforeAll non statique qui utilise les beans Spring
class BusinessRoutesIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(BusinessRoutesIntegrationTest.class);

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private MonitorsRepository monitorsRepository;
    @Autowired private StudentsRepository studentsRepository;
    @Autowired private DrivingSchoolRegistryRepository registryRepository;
    @Autowired private DrivingSchoolService drivingSchoolService;
    @Autowired private JwtService jwtService;
    @Autowired private UserTenantResolver userTenantResolver;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EmailService emailService;

    /** Plus petite image PNG valide : la signature binaire est vérifiée à l'envoi. */
    private static final byte[] TINY_PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    /** Suffixe unique de cette exécution (ex : "3f9a1c2b"). */
    private final String run = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    /** Comptes créés pendant le test : supprimés dans {@link #cleanUp()}. */
    private final List<UUID> createdUserIds = new ArrayList<>();
    /** Jetons révoqués par le test de déconnexion : leur empreinte est supprimée à la fin. */
    private final List<String> revokedTokens = new ArrayList<>();

    private String schema;
    private UUID registryId;
    private User monitorUser;
    private String monitorToken;
    private String studentToken;
    private UUID studentId;   // fiche élève DANS le tenant
    private UUID monitorId;   // fiche moniteur DANS le tenant

    // =====================================================================
    //  Mise en place / nettoyage
    // =====================================================================

    @BeforeAll
    void createTenantWithMonitorAndStudent() throws Exception {
        // 1. Moniteur fondateur + demande de création d'auto-école (schéma public)
        monitorUser = newUser(Role.MONITOR, "it-monitor");
        DrivingSchoolRegistry registry = new DrivingSchoolRegistry();
        registry.setSchoolName("IT Auto-Ecole " + run);
        registry.setSchemaName(TenantSchemas.fromSchoolName("IT Auto-Ecole " + run));
        registry.setEmail("contact-" + run + "@ecole-it.cm");
        registry.setPhoneNumber("+237699111222");
        registry.setAddress("Akwa");
        registry.setCity("Douala");
        registry.setCountry("Cameroun");
        registry.setDrivingSchoolStatus(DrivingSchoolStatus.PENDING);
        registry.setSlug("it-auto-ecole-" + run);
        registry.setDescription("Auto-école de test : permis B, cours du soir.");
        registry.setAdmin(monitorUser);
        registry = registryRepository.save(registry);
        registryId = registry.getId();
        schema = registry.getSchemaName();

        // 2. Approbation par la plateforme : création RÉELLE du schéma (Flyway) + fiche auto-école + moniteur
        drivingSchoolService.approveRegistry(registryId);
        monitorToken = tokenFor(monitorUser);
        assertEquals(schema, jwtService.extractTenant(monitorToken), "le jeton du moniteur porte son tenant");

        // 3. Un élève rejoint l'auto-école (demande puis acceptation par le moniteur, via HTTP)
        User studentUser = newUser(Role.STUDENT, "it-student");
        studentToken = joinSchoolAndGetToken(studentUser);

        JsonNode me = json(perform(get("/api/students/me"), studentToken).andExpect(status().isOk()));
        studentId = UUID.fromString(me.get("id").asText());

        // 4. Le moniteur complète le dossier : permis B (nécessaire pour les examens)
        perform(put("/api/students/" + studentId), monitorToken, "{\"licenseCategory\":\"B\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenseCategory").value("B"));

        JsonNode monitors = json(perform(get("/api/monitors"), monitorToken).andExpect(status().isOk()));
        for (JsonNode monitor : monitors) {
            if (monitorUser.getId().toString().equals(monitor.get("userId").asText())) {
                monitorId = UUID.fromString(monitor.get("id").asText());
            }
        }
        assertNotNull(monitorId, "le moniteur fondateur doit exister dans le schéma de l'auto-école");
    }

    /**
     * Supprime le schéma et les lignes du schéma public créées par ce test, pour ne pas encombrer
     * la base de développement. Chaque étape est indépendante : un échec est seulement journalisé.
     */
    @AfterAll
    void cleanUp() {
        if (schema != null && TenantSchemas.isValid(schema)) {
            quietly("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
            quietly("DELETE FROM public.tenants WHERE code = ?", schema);
        }
        for (String token : revokedTokens) {
            quietly("DELETE FROM public.revoked_tokens WHERE token_hash = ?", sha256(token));
        }
        for (UUID userId : createdUserIds) {
            quietly("DELETE FROM public.school_join_request WHERE user_id = ?", userId);
        }
        if (registryId != null) {
            quietly("DELETE FROM public.school_subscriptions WHERE registry_id = ?", registryId);
            quietly("DELETE FROM public.school_join_request WHERE driving_school_id = ?", registryId);
            quietly("DELETE FROM public.driving_school_registry WHERE id = ?", registryId);
        }
        for (UUID userId : createdUserIds) {
            quietly("DELETE FROM public.document_access_logs WHERE document_id IN "
                    + "(SELECT id FROM public.user_documents WHERE user_id = ?)", userId);
            quietly("DELETE FROM public.document_contents WHERE object_key IN "
                    + "(SELECT object_key FROM public.user_documents WHERE user_id = ?)", userId);
            quietly("DELETE FROM public.user_documents WHERE user_id = ?", userId);
            quietly("DELETE FROM public.monitors WHERE user_id = ?", userId);
            quietly("DELETE FROM public.students WHERE user_id = ?", userId);
            quietly("DELETE FROM public.email_verification_tokens WHERE user_id = ?", userId);
            quietly("DELETE FROM public.password_reset_tokens WHERE user_id = ?", userId);
            quietly("DELETE FROM public._users WHERE id = ?", userId);
        }
    }

    // =====================================================================
    //  Véhicules
    // =====================================================================

    /** CRUD complet d'un véhicule, immatriculation normalisée et doublon refusé (409). */
    @Test
    void vehicles_Crud_AndDuplicateMatriculation() throws Exception {
        String matriculation = "it " + run.substring(0, 4) + " ab";
        String expected = matriculation.toUpperCase();

        JsonNode created = json(perform(post("/api/vehicles"), monitorToken,
                vehicleJson(matriculation, "DISPOSABLE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matriculation").value(expected)));
        String id = created.get("id").asText();

        // Même immatriculation écrite autrement : conflit
        perform(post("/api/vehicles"), monitorToken, vehicleJson(expected, "DISPOSABLE"))
                .andExpect(status().isConflict());

        perform(get("/api/vehicles/" + id), studentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Toyota Yaris"));

        perform(put("/api/vehicles/" + id), monitorToken, vehicleJson(expected, "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("MAINTENANCE"));

        perform(get("/api/vehicles"), monitorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber());

        perform(delete("/api/vehicles/" + id), monitorToken).andExpect(status().isOk());
        perform(get("/api/vehicles/" + id), monitorToken).andExpect(status().isNotFound());
    }

    /** Un élève ne peut pas ajouter de véhicule ; données invalides : 400. */
    @Test
    void vehicles_StudentCannotCreate_AndInvalidBodyIsRejected() throws Exception {
        perform(post("/api/vehicles"), studentToken, vehicleJson("ST " + run, "DISPOSABLE"))
                .andExpect(status().isForbidden());

        perform(post("/api/vehicles"), monitorToken, "{\"matriculation\":\"\",\"model\":\"X\"}")
                .andExpect(status().isBadRequest());
    }

    // =====================================================================
    //  Cours
    // =====================================================================

    @Test
    void courses_Crud() throws Exception {
        JsonNode created = json(perform(post("/api/courses"), monitorToken,
                "{\"title\":\"Priorités\",\"content\":\"Priorité à droite\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Priorités")));
        String id = created.get("id").asText();

        // L'élève lit les cours, mais ne peut pas en publier
        perform(get("/api/courses/" + id), studentToken).andExpect(status().isOk());
        perform(get("/api/courses"), studentToken).andExpect(status().isOk());
        perform(post("/api/courses"), studentToken, "{\"title\":\"X\",\"content\":\"Y\"}")
                .andExpect(status().isForbidden());

        perform(put("/api/courses/" + id), monitorToken, "{\"title\":\"Signalisation\",\"content\":\"Panneaux\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Signalisation"));

        perform(delete("/api/courses/" + id), monitorToken).andExpect(status().isOk());
        perform(get("/api/courses/" + id), monitorToken).andExpect(status().isNotFound());
    }

    // =====================================================================
    //  Examens et inscriptions
    // =====================================================================

    @Test
    void exams_CreateAndRegisterStudent_WithCategoryAndDuplicateRules() throws Exception {
        String examB = json(perform(post("/api/exams"), monitorToken,
                "{\"dateExams\":\"2030-06-15T08:00:00\",\"category\":\"B\"}")
                .andExpect(status().isCreated())).get("id").asText();

        String registration = "{\"studentId\":\"" + studentId + "\"}";
        JsonNode inscription = json(perform(post("/api/exams/" + examB + "/inscriptions"), monitorToken, registration)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inscriptionStatus").value("INSCRIT")));

        // Double inscription : 409
        perform(post("/api/exams/" + examB + "/inscriptions"), monitorToken, registration)
                .andExpect(status().isConflict());

        perform(get("/api/exams/" + examB + "/inscriptions"), monitorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        perform(get("/api/exams/inscriptions/me"), studentToken).andExpect(status().isOk());

        perform(patch("/api/exams/inscriptions/" + inscription.get("id").asText() + "?status=REFUSE"), monitorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inscriptionStatus").value("REFUSE"));

        // Examen du permis A alors que l'élève prépare le permis B : 400
        String examA = json(perform(post("/api/exams"), monitorToken,
                "{\"dateExams\":\"2030-07-15T08:00:00\",\"category\":\"A\"}")
                .andExpect(status().isCreated())).get("id").asText();
        perform(post("/api/exams/" + examA + "/inscriptions"), monitorToken, registration)
                .andExpect(status().isBadRequest());

        // Date passée refusée ; un élève ne crée pas d'examen
        perform(post("/api/exams"), monitorToken, "{\"dateExams\":\"2000-01-01T08:00:00\",\"category\":\"B\"}")
                .andExpect(status().isBadRequest());
        perform(post("/api/exams"), studentToken, "{\"dateExams\":\"2030-06-15T08:00:00\",\"category\":\"B\"}")
                .andExpect(status().isForbidden());
    }

    // =====================================================================
    //  Réservations
    // =====================================================================

    /** Création, chevauchement (60 min) refusé, confirmation, annulation. */
    @Test
    void reservations_CreateOverlapConfirmCancel() throws Exception {
        String vehicleId = json(perform(post("/api/vehicles"), monitorToken,
                vehicleJson("RS " + run.substring(0, 4) + " CD", "DISPOSABLE"))
                .andExpect(status().isCreated())).get("id").asText();

        // L'élève réserve pour lui-même : PENDING
        JsonNode reservation = json(perform(post("/api/reservations"), studentToken,
                slotJson(null, vehicleId, "2030-01-10T09:00:00", "CONDUITE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationStatus").value("PENDING"))
                .andExpect(jsonPath("$.studentId").value(studentId.toString())));
        String reservationId = reservation.get("id").asText();

        // Même moniteur 30 minutes plus tard : chevauchement → 409
        perform(post("/api/reservations"), monitorToken,
                slotJson(studentId.toString(), vehicleId, "2030-01-10T09:30:00", "CONDUITE"))
                .andExpect(status().isConflict());

        // 60 minutes plus tard : créneau libre ; réservé par le moniteur → CONFIRMED
        perform(post("/api/reservations"), monitorToken,
                slotJson(studentId.toString(), vehicleId, "2030-01-10T10:00:00", "CONDUITE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"));

        // Leçon de conduite sans véhicule : 400
        perform(post("/api/reservations"), studentToken, slotJson(null, null, "2030-01-11T09:00:00", "CONDUITE"))
                .andExpect(status().isBadRequest());

        // Un élève ne confirme pas ; le moniteur confirme
        perform(patch("/api/reservations/" + reservationId + "/confirm"), studentToken)
                .andExpect(status().isForbidden());
        perform(patch("/api/reservations/" + reservationId + "/confirm"), monitorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"));

        // L'élève annule sa réservation
        perform(patch("/api/reservations/" + reservationId + "/cancel"), studentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationStatus").value("CANCELLED"));

        // Une fois annulé, le créneau de 9 h est de nouveau disponible
        perform(post("/api/reservations"), monitorToken,
                slotJson(studentId.toString(), vehicleId, "2030-01-10T09:00:00", "CONDUITE"))
                .andExpect(status().isCreated());

        perform(get("/api/reservations"), studentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    // =====================================================================
    //  Paiements
    // =====================================================================

    /**
     * Élève : Mobile Money via la passerelle SIMULATED (PENDING puis « Vérifier » → VALIDATE,
     * ou REJECTED pour un numéro finissant par 0000). Moniteur : espèces → VALIDATE.
     * Le récapitulatif augmente du total validé.
     */
    @Test
    void payments_MobileMoneyCashAndSummary() throws Exception {
        PaymentSummaryDto before = summary();

        // 1. MoMo de l'élève : PENDING, puis confirmé par la vérification
        JsonNode momo = json(perform(post("/api/payments"), studentToken,
                "{\"amount\":50000,\"method\":\"MOMO\",\"motif\":\"INSCRIPTION\",\"phoneNumber\":\"+237 677 11 22 33\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.provider").value("SIMULATED")));
        perform(post("/api/payments/" + momo.get("id").asText() + "/refresh"), studentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("VALIDATE"));

        // 2. Orange Money avec un numéro « refusé » par la simulation
        JsonNode om = json(perform(post("/api/payments"), studentToken,
                "{\"amount\":10000,\"method\":\"OM\",\"motif\":\"EXAMS\",\"phoneNumber\":\"+237 699 00 0000\"}")
                .andExpect(status().isCreated()));
        perform(post("/api/payments/" + om.get("id").asText() + "/refresh"), studentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("REJECTED"));

        // 3. Règles côté élève : pas d'espèces, numéro obligatoire
        perform(post("/api/payments"), studentToken, "{\"amount\":1000,\"method\":\"CASH\",\"motif\":\"EXAMS\"}")
                .andExpect(status().isBadRequest());
        perform(post("/api/payments"), studentToken, "{\"amount\":1000,\"method\":\"MOMO\",\"motif\":\"EXAMS\"}")
                .andExpect(status().isBadRequest());

        // 4. Moniteur : 25 000 FCFA reçus en espèces → validé immédiatement
        JsonNode cash = json(perform(post("/api/payments"), monitorToken,
                "{\"studentsId\":\"" + studentId + "\",\"amount\":25000,\"method\":\"CASH\",\"motif\":\"INSCRIPTION\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("VALIDATE"))
                .andExpect(jsonPath("$.provider").value("MANUAL")));

        // Un paiement déjà traité ne peut plus être validé ni rejeté
        perform(patch("/api/payments/" + cash.get("id").asText() + "/reject"), monitorToken)
                .andExpect(status().isBadRequest());

        // 5. Récapitulatif : +75 000 validés ; réservé au moniteur
        PaymentSummaryDto after = summary();
        assertEquals(0, after.totalValidated().subtract(before.totalValidated()).compareTo(new BigDecimal("75000")));
        perform(get("/api/payments/summary"), studentToken).andExpect(status().isForbidden());
    }

    /** Validation / rejet manuels d'un paiement en attente, et webhook non signé sans effet. */
    @Test
    void payments_ManualRejectAndUnsignedWebhook() throws Exception {
        JsonNode pending = json(perform(post("/api/payments"), studentToken,
                "{\"amount\":5000,\"method\":\"MOMO\",\"motif\":\"EXAMS\",\"phoneNumber\":\"+237 677 99 88 77\"}")
                .andExpect(status().isCreated()));
        String id = pending.get("id").asText();

        // Webhook sans signature valide : 200 (Campay ne doit pas réessayer) mais aucun effet
        mockMvc.perform(get("/api/webhooks/campay")
                        .param("status", "SUCCESSFUL")
                        .param("reference", pending.get("externalReference").asText())
                        .param("external_reference", schema + ":" + id)
                        .param("signature", "faux"))
                .andExpect(status().isOk());

        perform(patch("/api/payments/" + id + "/validate"), studentToken).andExpect(status().isForbidden());
        perform(patch("/api/payments/" + id + "/reject"), monitorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("REJECTED"));
        perform(patch("/api/payments/" + id + "/validate"), monitorToken).andExpect(status().isBadRequest());
    }

    // =====================================================================
    //  Adhésion à l'auto-école
    // =====================================================================

    /**
     * Demande d'adhésion : doublon refusé (409), un moniteur qui n'est pas le responsable ne peut pas
     * l'accepter (403), le responsable l'accepte (200) et l'élève obtient un jeton portant le tenant.
     */
    @Test
    void joinSchool_RequestThenApproveByOwner() throws Exception {
        User applicant = newUser(Role.STUDENT, "it-join");
        String publicToken = tokenFor(applicant);
        assertNull(jwtService.extractTenant(publicToken), "pas encore de tenant avant l'adhésion");

        String body = "{\"drivingSchoolId\":\"" + registryId + "\",\"role\":\"STUDENT\"}";
        perform(post("/api/join-school/public"), publicToken, body, false).andExpect(status().isOk());
        perform(post("/api/join-school/public"), publicToken, body, false).andExpect(status().isConflict());

        perform(get("/api/join-school/me"), publicToken, null, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].joinStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].drivingSchoolName").value("IT Auto-Ecole " + run));

        UUID requestId = pendingRequestOf(applicant);

        // Un moniteur sans auto-école (donc pas le responsable) : 403
        String outsiderToken = tokenFor(newUser(Role.MONITOR, "it-outsider"));
        perform(post("/api/join-school/admin/" + requestId + "/approve"), outsiderToken, null, false)
                .andExpect(status().isForbidden());

        // Un élève ne peut pas approuver
        perform(post("/api/join-school/admin/" + requestId + "/approve"), publicToken, null, false)
                .andExpect(status().isForbidden());

        perform(post("/api/join-school/admin/" + requestId + "/approve"), monitorToken, null, false)
                .andExpect(status().isOk());

        String memberToken = tokenFor(applicant);
        assertEquals(schema, jwtService.extractTenant(memberToken));
        perform(get("/api/students/me"), memberToken).andExpect(status().isOk());
        assertEquals(ProfileStatus.ACTIVE, userRepository.findByEmail(applicant.getEmail()).orElseThrow().getProfileStatus());
    }

    /** Le responsable peut aussi refuser une demande. */
    @Test
    void joinSchool_RejectByOwner() throws Exception {
        User applicant = newUser(Role.STUDENT, "it-reject");
        String publicToken = tokenFor(applicant);
        perform(post("/api/join-school/public"), publicToken,
                "{\"drivingSchoolId\":\"" + registryId + "\",\"role\":\"STUDENT\"}", false)
                .andExpect(status().isOk());

        UUID requestId = pendingRequestOf(applicant);
        perform(post("/api/join-school/admin/" + requestId + "/reject"), monitorToken, null, false)
                .andExpect(status().isOk());
        // Demande déjà traitée : 409
        perform(post("/api/join-school/admin/" + requestId + "/approve"), monitorToken, null, false)
                .andExpect(status().isConflict());

        assertNull(jwtService.extractTenant(tokenFor(applicant)), "un refus ne donne pas accès au tenant");
    }

    // =====================================================================
    //  Sécurité
    // =====================================================================

    @Test
    void security_MissingTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/vehicles").header("X-Tenant-ID", schema))
                .andExpect(status().isUnauthorized());
    }

    /** Jeton valide mais pour une AUTRE auto-école que l'en-tête X-Tenant-ID : 401. */
    @Test
    void security_TokenTenantDifferentFromHeaderIsUnauthorized() throws Exception {
        User fresh = userRepository.findByEmail(monitorUser.getEmail()).orElseThrow();
        String otherTenantToken = jwtService.generateToken(fresh, "ae_autre_ecole_" + run);

        perform(get("/api/vehicles"), otherTenantToken).andExpect(status().isUnauthorized());

        // Jeton sans tenant (utilisateur sans auto-école) : 401 aussi
        String noTenantToken = jwtService.generateToken(fresh, null);
        perform(get("/api/vehicles"), noTenantToken).andExpect(status().isUnauthorized());
    }

    @Test
    void security_StudentOnMonitorOnlyRouteIsForbidden() throws Exception {
        perform(get("/api/students"), studentToken).andExpect(status().isForbidden());
        perform(get("/api/students"), monitorToken).andExpect(status().isOk());
    }

    /** En-tête X-Tenant-ID mal formé (tentative d'injection SQL) : 400. */
    @Test
    void security_InvalidTenantHeaderIsBadRequest() throws Exception {
        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + monitorToken)
                        .header("X-Tenant-ID", "x;DROP SCHEMA public"))
                .andExpect(status().isBadRequest());
    }

    /** Après POST /api/auth/logout, le même jeton est refusé partout (401). */
    @Test
    void security_LogoutRevokesToken() throws Exception {
        // Utilisateur dédié : le jeton révoqué ne doit pas être celui des autres tests
        String token = joinSchoolAndGetToken(newUser(Role.STUDENT, "it-logout"));

        perform(get("/api/auth/me"), token, null, false).andExpect(status().isOk());
        perform(get("/api/vehicles"), token).andExpect(status().isOk());

        perform(post("/api/auth/logout"), token, null, false).andExpect(status().isOk());
        revokedTokens.add(token);

        perform(get("/api/auth/me"), token, null, false).andExpect(status().isUnauthorized());
        perform(get("/api/vehicles"), token).andExpect(status().isUnauthorized());
    }

    // =====================================================================
    //  Outils
    // =====================================================================

    // =====================================================================
    //  Justificatifs (CNI, CAPEC) et abonnement
    // =====================================================================

    /**
     * Un moniteur qui veut rejoindre une auto-école envoie d'abord sa pièce d'identité et son CAPEC.
     * Le responsable les consulte (fichier identique à l'original, consultation journalisée), personne
     * d'autre ; ils passent à « vérifié » quand il accepte la demande et ne peuvent plus être supprimés.
     */
    @Test
    void documents_MonitorJoin_DocumentsVisibleToOwnerOnly() throws Exception {
        User applicant = newUser(Role.MONITOR, "it-join-monitor");
        String token = tokenFor(applicant);
        String body = "{\"drivingSchoolId\":\"" + registryId + "\",\"role\":\"MONITOR\"}";

        // Sans justificatif : refus avec un message qui dit quoi faire
        perform(post("/api/join-school/public"), token, body, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CAPEC")));
        perform(get("/api/documents/requirements"), token, null, false)
                .andExpect(jsonPath("$.missing.length()").value(2));

        upload(token, "CNI");
        upload(token, "CAPEC");
        perform(post("/api/join-school/public"), token, body, false).andExpect(status().isOk());
        UUID requestId = pendingRequestOf(applicant);

        JsonNode mine = json(perform(get("/api/documents/me"), token, null, false).andExpect(status().isOk()));
        assertEquals(2, mine.size());
        String documentId = mine.get(0).get("id").asText();
        byte[] own = perform(get("/api/documents/" + documentId + "/file"), token, null, false)
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
                .andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(TINY_PNG, own, "le fichier déchiffré est identique à l'original");

        // Un autre utilisateur ne peut pas le lire par la route « mes documents »
        perform(get("/api/documents/" + documentId + "/file"), studentToken, null, false).andExpect(status().isNotFound());

        // Le responsable les voit depuis la demande ; un moniteur sans auto-école, non
        JsonNode viaRequest = json(perform(get("/api/join-school/admin/" + requestId + "/documents"), monitorToken, null, false)
                .andExpect(status().isOk()));
        assertEquals(2, viaRequest.size());
        byte[] viaOwner = perform(get("/api/join-school/admin/" + requestId + "/documents/" + documentId + "/file"),
                monitorToken, null, false).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(TINY_PNG, viaOwner);
        String outsider = tokenFor(newUser(Role.MONITOR, "it-doc-outsider"));
        perform(get("/api/join-school/admin/" + requestId + "/documents"), outsider, null, false)
                .andExpect(status().isForbidden());

        perform(post("/api/join-school/admin/" + requestId + "/approve"), monitorToken, null, false)
                .andExpect(status().isOk());
        perform(get("/api/documents/me"), token, null, false)
                .andExpect(jsonPath("$[0].status").value("VERIFIED"))
                .andExpect(jsonPath("$[1].status").value("VERIFIED"));
        perform(delete("/api/documents/" + documentId), token, null, false).andExpect(status().isConflict());

        Integer logged = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.document_access_logs WHERE document_id = ?::uuid", Integer.class, documentId);
        assertTrue(logged != null && logged >= 2, "chaque consultation est journalisée");
        String fileNameInDb = jdbcTemplate.queryForObject(
                "SELECT file_name_enc FROM public.user_documents WHERE id = ?::uuid", String.class, documentId);
        assertTrue(fileNameInDb.startsWith("v1:"), "nom du fichier chiffré en base");
    }

    /** Page publique d'une auto-école validée : accessible sans compte, par son adresse lisible ; 404 sinon. */
    @Test
    void publicSchoolPage_BySlug() throws Exception {
        mockMvc.perform(get("/api/driving-schools/public/it-auto-ecole-" + run))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IT Auto-Ecole " + run))
                .andExpect(jsonPath("$.slug").value("it-auto-ecole-" + run))
                .andExpect(jsonPath("$.description").value("Auto-école de test : permis B, cours du soir."))
                .andExpect(jsonPath("$.admin").doesNotExist());
        mockMvc.perform(get("/api/driving-schools/public/auto-ecole-qui-n-existe-pas"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/driving-schools/public/all"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$[?(@.slug == 'it-auto-ecole-" + run + "')]").exists());
    }

    /** Stockage en base : le contenu est chiffré, et remplacer un justificatif supprime l'ancien fichier. */
    @Test
    void documents_DatabaseStorage_EncryptedAndReplacedWithoutOrphan() throws Exception {
        String token = tokenFor(newUser(Role.MONITOR, "it-replace"));
        String firstId = json(mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "cni.png", "image/png", TINY_PNG)).param("type", "CNI")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())).get("id").asText();
        String firstKey = jdbcTemplate.queryForObject(
                "SELECT object_key FROM public.user_documents WHERE id = ?::uuid", String.class, firstId);
        byte[] stored = jdbcTemplate.queryForObject(
                "SELECT content FROM public.document_contents WHERE object_key = ?", byte[].class, firstKey);
        assertFalse(java.util.Arrays.equals(TINY_PNG, stored), "contenu chiffré dans la base");

        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "cni2.png", "image/png", TINY_PNG)).param("type", "CNI")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.document_contents WHERE object_key = ?", Integer.class, firstKey);
        assertEquals(0, remaining, "l'ancien fichier est supprimé après le remplacement");
    }

    /** Aucun justificatif n'est demandé aux élèves : rien à envoyer, et l'envoi est refusé. */
    @Test
    void documents_StudentNeedsNoDocument() throws Exception {
        User newcomer = newUser(Role.STUDENT, "it-nodoc");
        perform(get("/api/documents/requirements"), tokenFor(newcomer), null, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.required").isEmpty());
        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "cni.png", "image/png", TINY_PNG))
                        .param("type", "CNI")
                        .header("Authorization", "Bearer " + tokenFor(newcomer)))
                .andExpect(status().isBadRequest());
        // Un fichier qui n'est ni une image ni un PDF est refusé (moniteur)
        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "cni.png", "image/png", "<html>".repeat(5).getBytes()))
                        .param("type", "CNI")
                        .header("Authorization", "Bearer " + tokenFor(newUser(Role.MONITOR, "it-badfile"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Format")));
    }

    /**
     * Le responsable ajoute un moniteur avec sa CNI et son CAPEC ; le moniteur active son compte avec le lien
     * d'invitation, se connecte et arrive directement dans l'auto-école.
     */
    @Test
    void monitors_OwnerAddsMonitor_InvitationActivatesAccount() throws Exception {
        String email = "it-invite-" + run + "@test.cm";
        MockMultipartFile monitorPart = new MockMultipartFile("monitor", "", MediaType.APPLICATION_JSON_VALUE,
                ("{\"firstname\":\"Paul\",\"lastname\":\"Invite\",\"email\":\"" + email + "\","
                        + "\"phoneNumber\":\"+237690000001\",\"gender\":\"MALE\",\"nationality\":\"Camerounaise\","
                        + "\"residenceCity\":\"Douala\",\"dateOfBirth\":\"1990-01-01\"}").getBytes(StandardCharsets.UTF_8));
        MockMultipartFile cni = new MockMultipartFile("cni", "cni.png", "image/png", TINY_PNG);
        MockMultipartFile capec = new MockMultipartFile("capec", "capec.png", "image/png", TINY_PNG);

        // Un élève ne peut pas ajouter de moniteur
        mockMvc.perform(multipart("/api/monitors").file(monitorPart).file(cni).file(capec)
                        .header("Authorization", "Bearer " + studentToken).header("X-Tenant-ID", schema))
                .andExpect(status().isForbidden());

        mockMvc.perform(multipart("/api/monitors").file(monitorPart).file(cni).file(capec)
                        .header("Authorization", "Bearer " + monitorToken).header("X-Tenant-ID", schema))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));
        User invited = userRepository.findByEmail(email).orElseThrow();
        createdUserIds.add(invited.getId());
        assertEquals(ProfileStatus.REGISTERED, invited.getProfileStatus());

        // Même adresse une seconde fois : 409
        mockMvc.perform(multipart("/api/monitors").file(monitorPart).file(cni).file(capec)
                        .header("Authorization", "Bearer " + monitorToken).header("X-Tenant-ID", schema))
                .andExpect(status().isConflict());

        Integer verified = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.user_documents WHERE user_id = ? AND status = 'VERIFIED'", Integer.class,
                invited.getId());
        assertEquals(2, verified, "CNI et CAPEC vérifiés par le responsable");
        String invitation = jdbcTemplate.queryForObject(
                "SELECT token FROM public.password_reset_tokens WHERE user_id = ?", String.class, invited.getId());
        org.mockito.Mockito.verify(emailService).sendMonitorInvitationEmail(
                org.mockito.ArgumentMatchers.eq(email), org.mockito.ArgumentMatchers.eq("Paul"),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(invitation));

        // Avant l'invitation, impossible de se connecter
        perform(post("/api/auth/login"), null,
                "{\"email\":\"" + email + "\",\"password\":\"MonMotDePasse1\"}", false)
                .andExpect(status().isUnauthorized());

        perform(post("/api/auth/accept-invitation"), null,
                "{\"token\":\"" + invitation + "\",\"password\":\"MonMotDePasse1\",\"acceptPrivacyPolicy\":true}", false)
                .andExpect(status().isOk());
        perform(post("/api/auth/accept-invitation"), null,
                "{\"token\":\"" + invitation + "\",\"password\":\"MonMotDePasse1\",\"acceptPrivacyPolicy\":true}", false)
                .andExpect(status().isBadRequest());

        JsonNode login = json(perform(post("/api/auth/login"), null,
                "{\"email\":\"" + email + "\",\"password\":\"MonMotDePasse1\"}", false)
                .andExpect(status().isAccepted()));
        String token = login.get("token").asText();
        assertEquals(schema, jwtService.extractTenant(token), "le moniteur invité arrive dans son auto-école");
        perform(get("/api/monitors/me"), token).andExpect(status().isOk());
    }

    /** Back-office : justificatifs du fondateur (consultation, refus motivé) et période d'essai de 15 jours. */
    @Test
    void platform_FounderDocumentsReview_AndTrialSubscription() throws Exception {
        String documentId = json(mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "cni.png", "image/png", TINY_PNG))
                        .param("type", "CNI")
                        .param("documentNumber", "IT123456")
                        .header("Authorization", "Bearer " + monitorToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentNumberMasked").value("••••3456"))).get("id").asText();

        var admin = org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .user("reviewer@drivehub.cm").roles("REVIEWER");

        mockMvc.perform(get("/api/platform/registries/" + registryId + "/documents").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(documentId));
        byte[] content = mockMvc.perform(get("/api/platform/documents/" + documentId + "/file").with(admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(TINY_PNG, content);

        // Refus sans motif : 400 ; avec motif : l'utilisateur est prévenu par email
        mockMvc.perform(patch("/api/platform/documents/" + documentId + "/review").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/platform/documents/" + documentId + "/review").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\",\"comment\":\"Photo floue\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
        org.mockito.Mockito.verify(emailService).sendDocumentRejectedEmail(
                org.mockito.ArgumentMatchers.eq(monitorUser.getEmail()), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("Photo floue"));
        perform(get("/api/documents/requirements"), monitorToken, null, false)
                .andExpect(jsonPath("$.missing[0]").value("CNI"));

        // Un utilisateur ordinaire n'accède pas au back-office
        perform(get("/api/platform/documents/" + documentId + "/file"), studentToken, null, false)
                .andExpect(status().isForbidden());

        // Période d'essai démarrée à l'approbation de l'auto-école (aucune facturation)
        perform(get("/api/driving-schools/me/subscription"), monitorToken, null, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TRIAL"))
                .andExpect(jsonPath("$.trialDaysLeft").value(15))
                .andExpect(jsonPath("$.billingEnabled").value(false));
    }

    /** Crée un compte (email vérifié) et son profil public, comme après une inscription. */
    private User newUser(Role role, String prefix) {
        User user = new User();
        user.setFirstname("Test");
        user.setLastname(prefix);
        user.setEmail(prefix + "-" + run + "-" + UUID.randomUUID().toString().substring(0, 6) + "@test.cm");
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setRoles(role);
        user.setProfileStatus(ProfileStatus.EMAIL_VERIFIED);
        user.setFullProfile(false);
        user = userRepository.save(user);
        createdUserIds.add(user.getId());

        if (role == Role.MONITOR) {
            Monitor profile = new Monitor();
            profile.setUser(user);
            profile.setPhoneNumber("+237699000000");
            profile.setDateOfBirth(new Date());
            profile.setGender(Gender.FEMALE);
            profile.setNationality("Camerounaise");
            profile.setResidenceCity("Douala");
            monitorsRepository.save(profile);
        } else {
            Student profile = new Student();
            profile.setUser(user);
            profile.setPhoneNumber("+237677000000");
            profile.setDateOfBirth(new Date());
            profile.setGender(Gender.MALE);
            profile.setNationality("Camerounaise");
            profile.setResidenceCity("Douala");
            studentsRepository.save(profile);
        }
        return user;
    }

    /** Jeton JWT tel que le produit la connexion : le tenant est calculé par le serveur. */
    private String tokenFor(User user) {
        User fresh = userRepository.findByEmail(user.getEmail()).orElseThrow();
        return jwtService.generateToken(fresh, userTenantResolver.resolveTenant(fresh).orElse(null));
    }

    /** Parcours complet d'adhésion d'un élève, puis jeton portant le tenant. */
    private String joinSchoolAndGetToken(User student) throws Exception {
        perform(post("/api/join-school/public"), tokenFor(student),
                "{\"drivingSchoolId\":\"" + registryId + "\",\"role\":\"STUDENT\"}", false)
                .andExpect(status().isOk());
        UUID requestId = pendingRequestOf(student);
        perform(post("/api/join-school/admin/" + requestId + "/approve"), monitorToken, null, false)
                .andExpect(status().isOk());
        String token = tokenFor(student);
        assertEquals(schema, jwtService.extractTenant(token));
        return token;
    }

    /** Envoie un justificatif (petite image PNG) pour l'utilisateur du jeton. */
    private void upload(String token, String type) throws Exception {
        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", type.toLowerCase() + ".png", "image/png", TINY_PNG))
                        .param("type", type)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    /** Identifiant de la demande en attente de l'utilisateur, vue par le moniteur responsable. */
    private UUID pendingRequestOf(User user) throws Exception {
        JsonNode page = json(perform(get("/api/join-school/admin/pending?size=100"), monitorToken, null, false)
                .andExpect(status().isOk()));
        for (JsonNode item : page.get("content")) {
            if (user.getEmail().equals(item.get("userEmail").asText())) {
                return UUID.fromString(item.get("requestId").asText());
            }
        }
        throw new AssertionError("Demande en attente introuvable pour " + user.getEmail());
    }

    private PaymentSummaryDto summary() throws Exception {
        return objectMapper.treeToValue(
                json(perform(get("/api/payments/summary"), monitorToken).andExpect(status().isOk())),
                PaymentSummaryDto.class);
    }

    /** Requête sur une route du tenant (jeton + X-Tenant-ID), sans corps. */
    private ResultActions perform(MockHttpServletRequestBuilder request, String token) throws Exception {
        return perform(request, token, null, true);
    }

    /** Requête sur une route du tenant (jeton + X-Tenant-ID), avec un corps JSON. */
    private ResultActions perform(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
        return perform(request, token, body, true);
    }

    /**
     * @param withTenant {@code false} pour les routes du schéma public (/api/auth, /api/join-school)
     */
    private ResultActions perform(MockHttpServletRequestBuilder request, String token, String body,
                                  boolean withTenant) throws Exception {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (withTenant) {
            request.header("X-Tenant-ID", schema);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private JsonNode json(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private static String vehicleJson(String matriculation, String state) {
        return "{\"matriculation\":\"" + matriculation + "\",\"model\":\"Toyota Yaris\",\"state\":\"" + state + "\"}";
    }

    private String slotJson(String student, String vehicleId, String dateTime, String type) {
        return "{"
                + (student == null ? "" : "\"studentId\":\"" + student + "\",")
                + "\"monitorId\":\"" + monitorId + "\","
                + (vehicleId == null ? "" : "\"vehicleId\":\"" + vehicleId + "\",")
                + "\"dateTime\":\"" + dateTime + "\",\"types\":\"" + type + "\"}";
    }

    private void quietly(String sql, Object... args) {
        try {
            jdbcTemplate.update(sql, args);
        } catch (Exception e) {
            log.warn("Nettoyage incomplet ({}) : {}", sql, e.getMessage());
        }
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
