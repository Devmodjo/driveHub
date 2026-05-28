package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
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


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

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
    public void sendAdminWelcomeMail(String toEmail, String name) {

        try {
            Context context = new Context();
            context.setVariable("name", name);

            String html = templateEngine.process("emails/admin-registry", context);

            sendHtmlEmail(toEmail, "Bienvenu sur le Back-office — Drivehub", html);

        } catch (Exception e) {
            log.error("Échec envoi email reset à {} : {}", toEmail, e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject,
                               String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper( message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

}
