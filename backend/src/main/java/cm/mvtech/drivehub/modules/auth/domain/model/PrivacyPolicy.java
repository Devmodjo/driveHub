package cm.mvtech.drivehub.modules.auth.domain.model;

/**
 * Version de la politique de confidentialité en vigueur.
 *
 * <p>À modifier à chaque changement important du texte publié sur /confidentialite
 * (frontend : components/vitrine/pages/privacy). La version acceptée est enregistrée
 * sur chaque compte à l'inscription (colonne _users.privacy_policy_version).</p>
 */
public final class PrivacyPolicy {

    public static final String CURRENT_VERSION = "2026-10-02";

    private PrivacyPolicy() {
    }
}
