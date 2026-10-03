package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.MailRecipient;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Emails : le vrai modèle Thymeleaf est utilisé ; seul l'envoi SMTP est simulé.
 * On vérifie ce qui compte pour l'affichage et la délivrabilité.
 */
class EmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private EmailService service;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        service = new EmailService(mailSender, engine);
        ReflectionTestUtils.setField(service, "fromEmail", "no-reply@drivehub.cm");
        ReflectionTestUtils.setField(service, "fromName", "DriveHub");
        ReflectionTestUtils.setField(service, "replyTo", "contact@drivehub.cm");
        ReflectionTestUtils.setField(service, "supportEmail", "support@drivehub.cm");
        ReflectionTestUtils.setField(service, "frontendUrl", "https://app.drivehub.cm");
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));
    }

    /** Message envoyé, finalisé comme le fait l'envoi SMTP réel (en-têtes Content-Type calculés). */
    private MimeMessage sent() throws Exception {
        ArgumentCaptor<MimeMessage> message = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(message.capture());
        message.getValue().saveChanges();
        return message.getValue();
    }

    /** Toutes les parties « feuilles » du message (texte, HTML, logo). */
    private static List<Part> leaves(Part part) throws Exception {
        List<Part> result = new ArrayList<>();
        if (part.getContent() instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart child = multipart.getBodyPart(i);
                result.addAll(leaves(child));
            }
        } else {
            result.add(part);
        }
        return result;
    }

    private static String partOfType(MimeMessage message, String type) throws Exception {
        for (Part part : leaves(message)) {
            if (part.isMimeType(type)) {
                return part.getContent().toString();
            }
        }
        return null;
    }

    @Test
    void verificationEmail_HasHtmlTextLogoAndHeaders() throws Exception {
        service.sendVerificationEmail("awa@test.cm", "Awa", "https://app.drivehub.cm/verify-email?token=abc");
        MimeMessage message = sent();

        assertEquals("Confirmez votre adresse email", message.getSubject());
        assertEquals("DriveHub <no-reply@drivehub.cm>", message.getFrom()[0].toString());
        assertEquals("contact@drivehub.cm", message.getReplyTo()[0].toString());

        String html = partOfType(message, "text/html");
        String text = partOfType(message, "text/plain");
        assertNotNull(html, "version HTML");
        assertNotNull(text, "version texte (critère anti-spam)");
        assertTrue(html.contains("cid:logo"), "logo DriveHub joint");
        assertTrue(html.contains("Bonjour <span>Awa</span>,"));
        assertTrue(html.contains("https://app.drivehub.cm/verify-email?token=abc"));
        assertTrue(text.contains("Bonjour Awa,"));
        assertTrue(text.contains("https://app.drivehub.cm/verify-email?token=abc"));

        boolean logo = false;
        for (Part part : leaves(message)) {
            if (part.isMimeType("image/png")) {
                logo = true;
                assertArrayEquals(new String[]{"<logo>"}, part.getHeader("Content-ID"));
            }
        }
        assertTrue(logo, "le logo est joint à l'email");
    }

    @Test
    void subjects_ContainNoEmoji() throws Exception {
        service.sendAdminEmailVerification("a@test.cm", "Awa", "a@test.cm", "REVIEWER", "https://x");
        service.sendNewAdminRegistrationNotification("root@test.cm", "Awa", "a@test.cm", "REVIEWER", "Douala",
                null, "Motif", "03/10/2026", null);
        ArgumentCaptor<MimeMessage> messages = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(2)).send(messages.capture());
        for (MimeMessage message : messages.getAllValues()) {
            String subject = message.getSubject();
            assertTrue(subject.codePoints().noneMatch(cp -> cp > 0x2000 && Character.getType(cp) == Character.OTHER_SYMBOL),
                    "objet sans emoji : " + subject);
        }
    }

    @Test
    void detailsAndHighlight_AreRendered_AndMissingValueIsExplicit() throws Exception {
        service.sendAdminWelcomeMail("admin@test.cm", "Awa", "ACTIVE", "REVIEWER", "Temp-1234");
        String html = partOfType(sent(), "text/html");
        assertTrue(html.contains("Mot de passe temporaire"));
        assertTrue(html.contains("Temp-1234"));
        assertTrue(html.contains("https://app.drivehub.cm/backoffice/login"));

        reset(mailSender);
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));
        service.sendNewAdminRegistrationNotification("root@test.cm", "Awa", "a@test.cm", "REVIEWER", "Douala",
                null, "Motif", "03/10/2026", null);
        assertTrue(partOfType(sent(), "text/plain").contains("Téléphone : Non renseigné"));
    }

    @Test
    void announcement_IsPersonalizedAndHasListUnsubscribe() throws Exception {
        service.sendPlatformAnnouncement(List.of(new MailRecipient("awa@test.cm", "Awa")), "Nouveautés", "Bonjour à tous");
        MimeMessage message = sent();
        assertEquals("<mailto:support@drivehub.cm?subject=Desinscription>", message.getHeader("List-Unsubscribe")[0]);
        assertTrue(partOfType(message, "text/plain").contains("Bonjour Awa,"));
    }

    @Test
    void transactionalEmail_HasNoListUnsubscribe() throws Exception {
        service.sendJoinApprovedEmail("awa@test.cm", "Awa", "Auto-école Test");
        assertNull(sent().getHeader("List-Unsubscribe"));
    }

    @Test
    void sendFailure_IsLoggedNotThrown() {
        doThrow(new MailSendException("SMTP indisponible")).when(mailSender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> service.sendSchoolApprovedEmail("awa@test.cm", "Awa", "Auto-école Test"));
    }
}
