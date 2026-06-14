package cm.mvtech.drivehub.modules.auth.domain.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    private String frontendUrl = "https://localhost:4200";

    @Async
    public void sendVerificationEmail(String toEmail, String name,
                                      String verificationUrl) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationUrl", verificationUrl);

            String html = templateEngine.process(
                    "emails/verify-email", context);

            sendHtmlEmail(toEmail, "Vérifiez votre adresse email — DriveHub", html);
            log.info("Email de vérification envoyé à {}", toEmail);

        } catch (Exception e) {
            log.error("Échec envoi email vérification à {} : {}", toEmail, e.getMessage());
            // On ne lève pas l'exception — l'inscription ne doit pas échouer
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name,
                                       String resetUrl) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetUrl", resetUrl);

            String html = templateEngine.process(
                    "emails/reset-password", context);

            sendHtmlEmail(toEmail, "Réinitialisation de mot de passe — DriveHub", html);
            log.info("Email reset password envoyé à {}", toEmail);

        } catch (Exception e) {
            log.error("Échec envoi email reset à {} : {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendAdminWelcomeMail(String toEmail,
                                      String name,
                                      String status,
                                      String role,
                                      String temporaryPassword) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("email", toEmail);
            context.setVariable("status", status);
            context.setVariable("role", role);
            // null si inscription normale, mot de passe si créé par ROOT
            context.setVariable("temporaryPassword", temporaryPassword);

            String html = templateEngine.process("emails/admin-welcome", context);
            sendHtmlEmail(toEmail, "Bienvenue sur DriveHub — Votre compte administrateur", html);
            log.info("Email de bienvenue envoyé à {}", toEmail);
        } catch (Exception e) {
            log.error("Échec envoi email bienvenue à {} : {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendNewAdminRegistrationNotification(
            String rootEmail,
            String candidateName,
            String candidateEmail,
            String role,
            String residence,
            String phone,
            String reason,
            String registeredAt,
            UUID adminId) {
        try {
            Context context = new Context();
            context.setVariable("name", candidateName);
            context.setVariable("email", candidateEmail);
            context.setVariable("role", role);
            context.setVariable("residence", residence);
            context.setVariable("phone", phone);
            context.setVariable("reason", reason);
            context.setVariable("registeredAt", registeredAt);
            context.setVariable("activateUrl",
                    frontendUrl + "/backoffice/admin/" + adminId + "/activate");
            context.setVariable("backofficeUrl",
                    frontendUrl + "/backoffice/admins/pending");

            String html = templateEngine.process(
                    "emails/admin-new-registration", context);
            sendHtmlEmail(rootEmail,
                    "🔔 Nouvelle demande d'accès back-office — " + candidateName,
                    html);
            log.info("Notification ROOT envoyée pour la demande de {}",
                    candidateEmail);
        } catch (Exception e) {
            log.error("Échec notification ROOT : {}", e.getMessage());
        }
    }

    @Async
    public void sendAdminEmailVerification(String toEmail,
                                           String name,
                                           String email,
                                           String role,
                                           String verificationUrl) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("email", email);
            context.setVariable("role", role);
            context.setVariable("verificationUrl", verificationUrl);

            String html = templateEngine.process(
                    "emails/admin-verify-email", context);
            sendHtmlEmail(toEmail,
                    "🔐 Confirmez votre email — DriveHub Back-Office",
                    html);
            log.info("Email vérification admin envoyé à {}", toEmail);
        } catch (Exception e) {
            log.error("Échec email vérification admin {} : {}",
                    toEmail, e.getMessage());
        }
    }

    @Async
    public void sendAdminActivatedEmail(String toEmail, String name) {

        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("email", toEmail);

            String html = templateEngine.process("emails/admin-activated", context);
            sendHtmlEmail(toEmail, "Bienvenue sur DriveHub — Activation du Compte Admin", html);
            log.info("Email dd'activation de compte envoyé à {}", toEmail);

        } catch (Exception e) {
            log.error("Échec envoi email d'activation à {} : {}", toEmail, e.getMessage());
        }

    }

    private void sendHtmlEmail(String to, String subject,
                               String html) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper( message, true, "UTF-8");
        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

}
