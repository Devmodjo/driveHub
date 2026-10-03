package cm.mvtech.drivehub.modules.auth.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenu d'un email DriveHub, indépendant de sa mise en forme.
 *
 * <p>Tous les emails utilisent le même modèle (templates/emails/layout.html) : on décrit seulement
 * le contenu ici, et EmailService produit à partir de lui la version HTML ET la version texte
 * (les deux sont envoyées : les filtres anti-spam se méfient des emails sans version texte).</p>
 *
 * <p>Exemple :</p>
 * <pre>
 * EmailContent.builder("Vérifiez votre adresse email", "Confirmez votre adresse")
 *     .greeting("Awa")
 *     .paragraph("Merci de votre inscription sur DriveHub.")
 *     .action("Vérifier mon adresse", url)
 *     .note("Ce lien est valable 24 heures.")
 *     .build();
 * </pre>
 *
 * @param subject     objet de l'email (sans emoji ni majuscules excessives : critères anti-spam)
 * @param preheader   phrase d'aperçu affichée par la messagerie à côté de l'objet
 * @param title       titre affiché en haut de l'email
 * @param greetingName prénom pour « Bonjour {prénom}, » (null = « Bonjour, »)
 * @param paragraphs  paragraphes du message
 * @param details     tableau « libellé : valeur » (facultatif)
 * @param highlight   encadré mis en avant, ex : mot de passe temporaire (facultatif)
 * @param actionLabel texte du bouton (null = pas de bouton)
 * @param actionUrl   lien du bouton
 * @param notes       remarques en petit sous le bouton (validité du lien, sécurité...)
 * @param reason      pourquoi la personne reçoit cet email (pied de page)
 * @param bulk        email envoyé à un groupe (annonce) : ajoute l'en-tête List-Unsubscribe
 */
public record EmailContent(
        String subject,
        String preheader,
        String title,
        String greetingName,
        List<String> paragraphs,
        List<Detail> details,
        Detail highlight,
        String actionLabel,
        String actionUrl,
        List<String> notes,
        String reason,
        boolean bulk
) {

    /** Une ligne « libellé : valeur ». */
    public record Detail(String label, String value) {
    }

    public static Builder builder(String subject, String title) {
        return new Builder(subject, title);
    }

    /** Construction pas à pas (seuls l'objet et le titre sont obligatoires). */
    public static final class Builder {
        private final String subject;
        private final String title;
        private String preheader;
        private String greetingName;
        private final List<String> paragraphs = new ArrayList<>();
        private final List<Detail> details = new ArrayList<>();
        private Detail highlight;
        private String actionLabel;
        private String actionUrl;
        private final List<String> notes = new ArrayList<>();
        private String reason = "Vous recevez cet email car vous avez un compte sur DriveHub.";
        private boolean bulk;

        private Builder(String subject, String title) {
            this.subject = subject;
            this.title = title;
        }

        public Builder preheader(String preheader) { this.preheader = preheader; return this; }
        public Builder greeting(String name) { this.greetingName = name; return this; }
        public Builder paragraph(String text) { paragraphs.add(text); return this; }
        public Builder detail(String label, String value) {
            details.add(new Detail(label, value == null || value.isBlank() ? "Non renseigné" : value));
            return this;
        }
        public Builder highlight(String label, String value) { this.highlight = new Detail(label, value); return this; }
        public Builder action(String label, String url) { this.actionLabel = label; this.actionUrl = url; return this; }
        public Builder note(String text) { notes.add(text); return this; }
        public Builder reason(String text) { this.reason = text; return this; }
        public Builder bulk() { this.bulk = true; return this; }

        public EmailContent build() {
            boolean hasAction = actionLabel != null && actionUrl != null;
            return new EmailContent(subject, preheader != null ? preheader : title, title, greetingName,
                    List.copyOf(paragraphs), List.copyOf(details), highlight,
                    hasAction ? actionLabel : null, hasAction ? actionUrl : null,
                    List.copyOf(notes), reason, bulk);
        }
    }
}
