package cm.mvtech.drivehub.core.infrastructure;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Règles de nommage des schémas PostgreSQL des tenants (une auto-école = un schéma).
 *
 * <p>Un nom de schéma est inséré tel quel dans du SQL ({@code SET search_path TO ...},
 * {@code CREATE SCHEMA ...}) : il ne peut pas être passé en paramètre JDBC. Il doit donc
 * être strictement validé, sinon un en-tête HTTP ou un nom d'auto-école malveillant
 * permettrait une injection SQL.</p>
 */
public final class TenantSchemas {

    public static final String PUBLIC = "public";

    /** Préfixe : garantit que le nom commence par une lettre et ne heurte aucun mot réservé SQL. */
    private static final String PREFIX = "ae_";

    /** Lettres minuscules, chiffres, underscore ; 63 caractères max (limite PostgreSQL). */
    private static final Pattern VALID = Pattern.compile("^[a-z][a-z0-9_]{1,62}$");

    private TenantSchemas() {
    }

    public static boolean isValid(String schema) {
        return schema != null && VALID.matcher(schema).matches();
    }

    /** Lève une exception si le nom n'est pas un identifiant de schéma sûr. */
    public static String requireValid(String schema) {
        if (!isValid(schema)) {
            throw new IllegalArgumentException("Nom de schéma tenant invalide : " + schema);
        }
        return schema;
    }

    /**
     * Génère un nom de schéma unique et sûr à partir du nom de l'auto-école.
     * Exemple : "Auto-École Le Volant" -> "ae_auto_ecole_le_volant_3f9a1c".
     * Le suffixe aléatoire évite les collisions ("Le Volant" et "Le-Volant" donneraient sinon le même schéma).
     */
    public static String fromSchoolName(String schoolName) {
        String ascii = Normalizer.normalize(schoolName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")              // supprime les accents
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (ascii.length() > 40) {
            ascii = ascii.substring(0, 40);
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return requireValid(PREFIX + (ascii.isEmpty() ? "ecole" : ascii) + "_" + suffix);
    }
}
