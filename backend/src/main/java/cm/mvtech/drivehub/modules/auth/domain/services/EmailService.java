package cm.mvtech.drivehub.modules.auth.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.model.EmailContent;
import cm.mvtech.drivehub.modules.auth.domain.model.MailRecipient;
import cm.mvtech.drivehub.modules.messageapi.ValidationDelay;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Envoi de tous les emails de DriveHub.
 *
 * <h2>Fonctionnement</h2>
 * Chaque méthode publique décrit le CONTENU de l'email ({@link EmailContent}) ; la mise en forme est
 * commune ({@code templates/emails/layout.html}, avec le logo DriveHub). Les envois sont asynchrones
 * (@Async) et une erreur d'envoi est seulement journalisée : elle n'annule jamais l'action métier.
 *
 * <h2>Pour ne pas finir dans les spams</h2>
 * <ul>
 *   <li>chaque email contient une version HTML ET une version texte ;</li>
 *   <li>l'expéditeur (MAIL_FROM) est une adresse de NOTRE domaine, et son Message-ID aussi ;</li>
 *   <li>objets sans emoji ni majuscules, un seul lien d'action, un pied de page qui dit pourquoi on écrit ;</li>
 *   <li>les annonces groupées portent l'en-tête List-Unsubscribe ;</li>
 *   <li>le reste se règle dans le DNS du domaine (SPF, DKIM, DMARC) : voir docs/EMAILS-DELIVRABILITE.md.</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private static final String LOGO_CID = "logo";
    private static final ClassPathResource LOGO = new ClassPathResource("mail/logo-drivehub.png");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    /** Adresse de réponse (facultative) : une boîte lue par l'équipe, différente de l'expéditeur no-reply. */
    @Value("${app.mail.reply-to:}")
    private String replyTo;

    @Value("${app.mail.support-email:support@drivehub.cm}")
    private String supportEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // =====================================================================
    //  Compte utilisateur (élève, moniteur)
    // =====================================================================

    @Async
    public void sendVerificationEmail(String toEmail, String name, String verificationUrl) {
        send(toEmail, EmailContent.builder("Confirmez votre adresse email", "Confirmez votre adresse email")
                .preheader("Un clic pour activer votre compte DriveHub.")
                .greeting(name)
                .paragraph("Merci de vous être inscrit sur DriveHub. Pour activer votre compte, confirmez "
                        + "votre adresse email avec le bouton ci-dessous.")
                .action("Confirmer mon adresse", verificationUrl)
                .note("Ce lien est valable 24 heures. Passé ce délai, demandez-en un nouveau depuis la page de connexion.")
                .note("Vous n'avez pas créé de compte ? Ignorez cet email : votre adresse ne sera pas utilisée.")
                .reason("Vous recevez cet email car une inscription a été faite avec cette adresse sur DriveHub.")
                .build());
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetUrl) {
        send(toEmail, EmailContent.builder("Réinitialisation de votre mot de passe", "Choisissez un nouveau mot de passe")
                .preheader("Lien valable 1 heure.")
                .greeting(name)
                .paragraph("Nous avons reçu une demande de réinitialisation du mot de passe de votre compte DriveHub. "
                        + "Si elle vient de vous, choisissez un nouveau mot de passe avec le bouton ci-dessous.")
                .detail("Compte", toEmail)
                .detail("Validité du lien", "1 heure")
                .action("Choisir un nouveau mot de passe", resetUrl)
                .note("Vous n'avez rien demandé ? Ignorez cet email : votre mot de passe actuel reste valable.")
                .note("Conseil : au moins 8 caractères, et un mot de passe que vous n'utilisez nulle part ailleurs.")
                .reason("Vous recevez cet email car une réinitialisation de mot de passe a été demandée pour cette adresse.")
                .build());
    }

    // =====================================================================
    //  Administrateurs de la plateforme (back-office)
    // =====================================================================

    @Async
    public void sendAdminWelcomeMail(String toEmail, String name, String status, String role, String temporaryPassword) {
        boolean active = "ACTIVE".equals(status);
        EmailContent.Builder content = EmailContent.builder("Bienvenue sur le back-office DriveHub",
                        active ? "Votre compte administrateur est prêt" : "Votre demande d'accès est enregistrée")
                .greeting(name)
                .detail("Adresse email", toEmail)
                .detail("Rôle", role)
                .reason("Vous recevez cet email car un compte administrateur DriveHub a été créé avec cette adresse.");
        if (active) {
            content.paragraph("Votre compte administrateur DriveHub a été créé et activé. Vous pouvez vous connecter dès maintenant.");
            if (temporaryPassword != null) {
                content.highlight("Mot de passe temporaire", temporaryPassword)
                        .note("Changez ce mot de passe temporaire dès votre première connexion.");
            }
            content.action("Accéder au back-office", frontendUrl + "/backoffice/login");
        } else {
            content.paragraph("Nous avons bien reçu votre demande d'accès au back-office. Elle sera examinée par "
                    + "l'équipe DriveHub : vous recevrez un email dès qu'une décision sera prise.");
        }
        send(toEmail, content.note("Vous n'êtes pas à l'origine de cette demande ? Écrivez-nous à " + supportEmail + ".")
                .build());
    }

    @Async
    public void sendNewAdminRegistrationNotification(String rootEmail, String candidateName, String candidateEmail,
                                                     String role, String residence, String phone, String reason,
                                                     String registeredAt, UUID adminId) {
        send(rootEmail, EmailContent.builder("Nouvelle demande d'accès au back-office : " + candidateName,
                        "Nouvelle demande d'accès à examiner")
                .paragraph("Une demande d'accès au back-office vient d'être confirmée par email. Le compte reste "
                        + "inactif tant que vous ne l'avez pas activé.")
                .detail("Nom", candidateName)
                .detail("Adresse email", candidateEmail)
                .detail("Rôle demandé", role)
                .detail("Résidence", residence)
                .detail("Téléphone", phone)
                .detail("Date de la demande", registeredAt)
                .detail("Motif", reason)
                .action("Examiner la demande", frontendUrl + "/backoffice/dashboard/admins")
                .reason("Vous recevez cet email car vous êtes administrateur principal (ROOT) de DriveHub.")
                .build());
    }

    @Async
    public void sendAdminEmailVerification(String toEmail, String name, String email, String role, String verificationUrl) {
        send(toEmail, EmailContent.builder("Confirmez votre adresse email (back-office DriveHub)",
                        "Confirmez votre adresse email")
                .preheader("Première étape de votre demande d'accès au back-office.")
                .greeting(name)
                .paragraph("Vous avez demandé un accès au back-office DriveHub. Confirmez d'abord votre adresse email : "
                        + "votre demande sera ensuite transmise à l'équipe DriveHub, qui l'examinera.")
                .detail("Nom", name)
                .detail("Adresse email", email)
                .detail("Rôle demandé", role)
                .action("Confirmer mon adresse", verificationUrl)
                .note("Ce lien est valable 24 heures.")
                .note("Vous n'avez rien demandé ? Ignorez cet email : aucun accès n'est accordé sans validation manuelle.")
                .reason("Vous recevez cet email car une demande d'accès administrateur a été faite avec cette adresse.")
                .build());
    }

    @Async
    public void sendAdminActivatedEmail(String toEmail, String name) {
        send(toEmail, EmailContent.builder("Votre compte administrateur est activé", "Votre compte est activé")
                .greeting(name)
                .paragraph("Votre compte administrateur DriveHub vient d'être validé. Connectez-vous avec l'adresse "
                        + "email et le mot de passe choisis lors de votre inscription.")
                .action("Accéder au back-office", frontendUrl + "/backoffice/login")
                .reason("Vous recevez cet email car vous avez demandé un accès au back-office DriveHub.")
                .build());
    }

    // =====================================================================
    //  Auto-école et adhésion
    // =====================================================================

    /** Au moniteur : accusé de réception de sa demande de création d'auto-école. */
    @Async
    public void sendSchoolRequestReceivedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Demande reçue : " + schoolName,
                "Votre demande est bien enregistrée",
                "Nous avons bien reçu la demande de création de « " + schoolName + " ». "
                        + "Notre équipe vérifie chaque auto-école avant son ouverture sur DriveHub : "
                        + "votre demande sera traitée sous " + ValidationDelay.TEXT + ". "
                        + "Vous recevrez un email dès qu'elle sera validée.",
                "Suivre ma demande", frontendUrl + "/dashboard/bienvenue");
    }

    /** Au moniteur : sa demande de création d'auto-école est validée par la plateforme. */
    @Async
    public void sendSchoolApprovedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Votre auto-école est validée",
                "Votre auto-école est validée",
                "Bonne nouvelle : « " + schoolName + " » a été validée par l'équipe DriveHub. "
                        + "Votre espace de gestion est prêt : élèves, véhicules, réservations, examens et paiements. "
                        + "Votre période d'essai gratuite de 15 jours commence aujourd'hui.",
                "Accéder à mon espace", frontendUrl + "/dashboard/bienvenue");
    }

    /** Au moniteur : sa demande de création d'auto-école est refusée. */
    @Async
    public void sendSchoolRejectedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Votre demande de création d'auto-école",
                "Votre demande n'a pas été retenue",
                "La demande de création de « " + schoolName + " » n'a pas été validée. "
                        + "Écrivez-nous à " + supportEmail + " pour en connaître la raison et la soumettre à nouveau.",
                null, null);
    }

    /** Au responsable de l'auto-école : un élève (ou un moniteur) demande à la rejoindre. */
    @Async
    public void sendJoinRequestReceivedEmail(String toEmail, String name, String applicantName, String schoolName) {
        sendNotification(toEmail, name,
                "Nouvelle demande d'adhésion",
                "Nouvelle demande d'adhésion",
                applicantName + " souhaite rejoindre « " + schoolName + " ». "
                        + "Acceptez ou refusez sa demande depuis votre espace.",
                "Voir les demandes", frontendUrl + "/dashboard/demandes");
    }

    /** Au demandeur : son adhésion est acceptée. */
    @Async
    public void sendJoinApprovedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Bienvenue chez " + schoolName,
                "Votre inscription est acceptée",
                "« " + schoolName + " » a accepté votre demande. Vous pouvez maintenant réserver vos leçons, "
                        + "suivre vos cours, consulter vos examens et payer vos frais en ligne.",
                "Accéder à mon espace", frontendUrl + "/dashboard/bienvenue");
    }

    /** Au demandeur : son adhésion est refusée. */
    @Async
    public void sendJoinRejectedEmail(String toEmail, String name, String schoolName) {
        sendNotification(toEmail, name,
                "Votre demande d'adhésion",
                "Votre demande n'a pas été acceptée",
                "« " + schoolName + " » n'a pas accepté votre demande. Vous pouvez choisir une autre auto-école "
                        + "dans le catalogue DriveHub.",
                "Voir les auto-écoles", frontendUrl + "/auto-ecoles");
    }

    /** Au titulaire : un de ses justificatifs a été refusé. */
    @Async
    public void sendDocumentRejectedEmail(String toEmail, String name, String documentLabel, String reason) {
        send(toEmail, EmailContent.builder("Un justificatif est à renvoyer", "Un justificatif doit être renvoyé")
                .greeting(name)
                .paragraph("Nous n'avons pas pu valider " + documentLabel + ".")
                .detail("Raison", reason)
                .paragraph("Envoyez un nouveau fichier depuis votre espace : la vérification reprendra aussitôt.")
                .action("Envoyer un nouveau fichier", frontendUrl + "/dashboard/bienvenue")
                .build());
    }

    /**
     * Au moniteur ajouté par le responsable d'une auto-école : lien pour choisir son mot de passe
     * et accepter la politique de confidentialité (valable 72 heures).
     */
    @Async
    public void sendMonitorInvitationEmail(String toEmail, String name, String schoolName, String token) {
        send(toEmail, EmailContent.builder("Invitation à rejoindre " + schoolName + " sur DriveHub",
                        "Vous êtes invité à rejoindre " + schoolName)
                .preheader("Choisissez votre mot de passe pour activer votre compte moniteur.")
                .greeting(name)
                .paragraph("Le responsable de « " + schoolName + " » vous a ajouté comme moniteur sur DriveHub. "
                        + "Choisissez votre mot de passe pour accéder à votre espace : planning, élèves, cours et examens.")
                .action("Activer mon compte", frontendUrl + "/invitation?token=" + token)
                .note("Ce lien est valable 72 heures.")
                .note("Vous ne connaissez pas cette auto-école ? Ignorez cet email : le compte ne sera pas activé.")
                .reason("Vous recevez cet email car « " + schoolName + " » vous a ajouté comme moniteur sur DriveHub.")
                .build());
    }

    /**
     * Message libre envoyé depuis le back-office (annonce, information) à une liste de destinataires.
     *
     * <p>Exécuté en arrière-plan (@Async) : l'administrateur n'attend pas la fin de l'envoi.
     * Les emails partent un par un (personnalisés « Bonjour {nom} ») pour ménager le serveur SMTP ;
     * un échec sur une adresse n'empêche pas l'envoi aux suivantes.</p>
     */
    @Async
    public void sendPlatformAnnouncement(List<MailRecipient> recipients, String subject, String message) {
        int sent = 0;
        for (MailRecipient recipient : recipients) {
            boolean ok = send(recipient.email(), EmailContent.builder(subject, subject)
                    .greeting(recipient.name())
                    .paragraph(message)
                    .action("Ouvrir DriveHub", frontendUrl)
                    .note("Pour ne plus recevoir ces informations, répondez à cet email ou écrivez à " + supportEmail + ".")
                    .bulk()
                    .build());
            if (ok) {
                sent++;
            }
        }
        log.info("Annonce « {} » : {} / {} email(s) envoyé(s)", subject, sent, recipients.size());
    }

    // =====================================================================
    //  Mise en forme et envoi
    // =====================================================================

    /** Email simple : un message, un bouton facultatif. */
    private boolean sendNotification(String toEmail, String name, String subject, String title, String message,
                                     String actionLabel, String actionUrl) {
        return send(toEmail, EmailContent.builder(subject, title)
                .greeting(name)
                .paragraph(message)
                .action(actionLabel, actionUrl)
                .build());
    }

    /**
     * Met en forme et envoie un email. Une erreur est seulement journalisée : elle ne doit jamais annuler
     * l'action métier (une auto-école validée reste validée même si le serveur mail est indisponible).
     *
     * @return {@code true} si l'email est parti
     */
    boolean send(String to, EmailContent content) {
        try {
            MimeMessage message = newMessage();
            // MIXED_RELATED : versions texte + HTML, et le logo joint (affiché via cid:logo)
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }
            helper.setSubject(content.subject());
            helper.setText(toText(content), toHtml(content));
            helper.addInline(LOGO_CID, "logo-drivehub.png", LOGO, "image/png");
            if (content.bulk()) {
                // Lien de désinscription reconnu par Gmail, Outlook, Yahoo (critère des annonces groupées)
                message.setHeader("List-Unsubscribe", "<mailto:" + supportEmail + "?subject=Desinscription>");
            }
            mailSender.send(message);
            log.info("Email « {} » envoyé à {}", content.subject(), to);
            return true;
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
            log.error("Échec de l'envoi de l'email « {} » à {} : {}", content.subject(), to, e.getMessage());
            logLinkForLocalDevelopment(to, content);
            return false;
        }
    }

    /**
     * Développement uniquement (site sur http://localhost) : si l'email n'a pas pu partir (pas de serveur
     * SMTP configuré), le lien du bouton est écrit dans la console pour pouvoir continuer le test
     * (vérification d'adresse, invitation...). Jamais en production : le site n'y est pas sur localhost.
     */
    private void logLinkForLocalDevelopment(String to, EmailContent content) {
        if (content.actionUrl() != null && frontendUrl != null && frontendUrl.startsWith("http://localhost")) {
            log.warn("[DÉVELOPPEMENT] Lien de l'email « {} » pour {} : {}", content.subject(), to, content.actionUrl());
        }
    }

    /** Version HTML (modèle Thymeleaf commun). */
    String toHtml(EmailContent content) {
        Context context = new Context();
        context.setVariable("c", content);
        context.setVariable("siteUrl", frontendUrl);
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("fromEmail", fromEmail);
        context.setVariable("year", Year.now().getValue());
        return templateEngine.process("emails/layout", context);
    }

    /** Version texte, construite à partir du même contenu (lue par les filtres anti-spam et certaines messageries). */
    String toText(EmailContent content) {
        StringBuilder text = new StringBuilder();
        text.append(content.greetingName() != null ? "Bonjour " + content.greetingName() + "," : "Bonjour,").append("\n\n");
        text.append(content.title()).append("\n\n");
        content.paragraphs().forEach(p -> text.append(p).append("\n\n"));
        content.details().forEach(d -> text.append(d.label()).append(" : ").append(d.value()).append('\n'));
        if (!content.details().isEmpty()) {
            text.append('\n');
        }
        if (content.highlight() != null) {
            text.append(content.highlight().label()).append(" : ").append(content.highlight().value()).append("\n\n");
        }
        if (content.actionUrl() != null) {
            text.append(content.actionLabel()).append(" :\n").append(content.actionUrl()).append("\n\n");
        }
        content.notes().forEach(n -> text.append(n).append('\n'));
        text.append("\nL'équipe DriveHub\n\n--\n").append(content.reason()).append('\n')
                .append("DriveHub, plateforme de gestion d'auto-école - ").append(supportEmail).append('\n');
        return text.toString();
    }

    /**
     * Message dont l'identifiant (Message-ID) porte le domaine de l'expéditeur (ex : …@drivehub.cm)
     * plutôt que le nom de la machine du serveur : un détail vérifié par les filtres anti-spam.
     */
    private MimeMessage newMessage() {
        if (!(mailSender instanceof JavaMailSenderImpl impl)) {
            return mailSender.createMimeMessage();
        }
        Session session = impl.getSession();
        String domain = fromEmail != null && fromEmail.contains("@")
                ? fromEmail.substring(fromEmail.indexOf('@') + 1) : "drivehub.cm";
        return new MimeMessage(session) {
            @Override
            protected void updateMessageID() throws MessagingException {
                setHeader("Message-ID", "<" + UUID.randomUUID() + "@" + domain + ">");
            }
        };
    }
}
