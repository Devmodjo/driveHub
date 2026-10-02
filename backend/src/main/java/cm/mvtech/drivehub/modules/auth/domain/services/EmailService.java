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

    @Value("${app.frontend-url}")
    private String frontendUrl;

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
                    frontendUrl + "/backoffice/dashboard/admins");
            context.setVariable("backofficeUrl",
                    frontendUrl + "/backoffice/dashboard/admins");

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

    // =====================================================================
    //  Emails du workflow principal (auto-école et adhésion)
    //  Tous utilisent le même modèle : templates/emails/notification.html
    // =====================================================================

    /** Au moniteur : sa demande de création d'auto-école est validée par la plateforme. */
    @Async
    public void sendSchoolApprovedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Votre auto-école est validée — DriveHub",
                "Votre auto-école est validée",
                "Bonne nouvelle : « " + schoolName + " » a été validée par l'équipe DriveHub. "
                        + "Votre espace de gestion est prêt : élèves, véhicules, réservations, examens et paiements.",
                "Accéder à mon espace", frontendUrl + "/dashboard/bienvenue");
    }

    /** Au moniteur : sa demande de création d'auto-école est refusée. */
    @Async
    public void sendSchoolRejectedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Votre demande d'auto-école — DriveHub",
                "Votre demande n'a pas été retenue",
                "La demande de création de « " + schoolName + " » n'a pas été validée. "
                        + "Contactez le support DriveHub pour en connaître la raison et la soumettre à nouveau.",
                null, null);
    }

    /** Au responsable de l'auto-école : un élève (ou un moniteur) demande à la rejoindre. */
    @Async
    public void sendJoinRequestReceivedEmail(String toEmail, String name, String applicantName, String schoolName) {
        sendNotification(toEmail, name,
                "Nouvelle demande d'adhésion — DriveHub",
                "Nouvelle demande d'adhésion",
                applicantName + " souhaite rejoindre « " + schoolName + " ». "
                        + "Acceptez ou refusez sa demande depuis votre espace.",
                "Voir les demandes", frontendUrl + "/dashboard/demandes");
    }

    /** Au demandeur : son adhésion est acceptée. */
    @Async
    public void sendJoinApprovedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Bienvenue chez " + schoolName + " — DriveHub",
                "Votre inscription est acceptée",
                "« " + schoolName + " » a accepté votre demande. Vous pouvez maintenant réserver vos leçons, "
                        + "suivre vos cours, consulter vos examens et payer vos frais en ligne.",
                "Accéder à mon espace", frontendUrl + "/dashboard/bienvenue");
    }

    /** Au demandeur : son adhésion est refusée. */
    @Async
    public void sendJoinRejectedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Votre demande d'adhésion — DriveHub",
                "Votre demande n'a pas été acceptée",
                "« " + schoolName + " » n'a pas accepté votre demande. Vous pouvez choisir une autre auto-école "
                        + "dans le catalogue DriveHub.",
                "Voir les auto-écoles", frontendUrl + "/auto-ecoles");
    }

    /**
     * Construit et envoie un email à partir du modèle "notification".
     * Une erreur d'envoi est seulement journalisée : elle ne doit jamais annuler l'action métier
     * (une auto-école validée reste validée même si le serveur mail est indisponible).
     *
     * @param actionLabel texte du bouton (null = pas de bouton)
     * @param actionUrl   lien du bouton (null = pas de bouton)
     */
    private void sendNotification(String toEmail, String name, String subject, String title, String message,
                                  String actionLabel, String actionUrl) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("title", title);
            context.setVariable("message", message);
            context.setVariable("actionLabel", actionLabel);
            context.setVariable("actionUrl", actionUrl);

            String html = templateEngine.process("emails/notification", context);
            sendHtmlEmail(toEmail, subject, html);
            log.info("Email « {} » envoyé à {}", subject, toEmail);
        } catch (Exception e) {
            log.error("Échec envoi email « {} » à {} : {}", subject, toEmail, e.getMessage());
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
