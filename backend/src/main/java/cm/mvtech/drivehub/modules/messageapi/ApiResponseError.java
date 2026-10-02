package cm.mvtech.drivehub.modules.messageapi;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Corps de toutes les réponses d'erreur de l'API.
 *
 * <ul>
 *   <li>{@code message} : phrase lisible, affichable telle quelle par le frontend ;</li>
 *   <li>{@code fieldErrors} : seulement pour une erreur de formulaire (400), le message de chaque
 *       champ invalide, ex : {@code {"description": "La présentation ne doit pas dépasser 2000 caractères"}}.
 *       Le frontend l'affiche sous le champ concerné. Absent du JSON quand il n'y en a pas.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    /** Erreur sans détail par champ. */
    public ApiResponseError(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}
