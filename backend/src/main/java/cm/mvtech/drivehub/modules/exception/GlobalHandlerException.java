package cm.mvtech.drivehub.modules.exception;

import cm.mvtech.drivehub.modules.messageapi.ApiResponseError;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseError> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseError> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseError> handleConflictException(ConflictException ex, HttpServletRequest request) {
        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Formulaire invalide (@Valid) : 400 avec le message de chaque champ dans {@code fieldErrors}.
     * Le message principal reprend la première erreur, ou indique le nombre de champs à corriger.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        String message = errors.size() == 1
                ? errors.values().iterator().next()
                : errors.size() + " champs sont à corriger : " + String.join(" ; ", errors.values());

        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI(),
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Accès refusé (@PreAuthorize, règle métier). Sans ce handler, le handler générique
     * ci-dessous transformait les 403 en 500 (vos tests d'intégration le détectaient).
     */
    @ExceptionHandler({AccessDeniedException.class, IllegalAccessException.class})
    public ResponseEntity<ApiResponseError> handleAccessDenied(Exception ex, HttpServletRequest request) {
        // Pas de jeton valide (absent, expiré ou révoqué par un logout) : 401 « non authentifié ».
        // Le frontend s'appuie sur ce 401 pour fermer la session. Un utilisateur connecté mais sans
        // le bon rôle reçoit 403 « accès interdit ».
        if (isAnonymous()) {
            return build(HttpStatus.UNAUTHORIZED, "Authentification requise", request);
        }
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    private static boolean isAnonymous() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated();
    }

    /** Identifiants invalides, compte suspendu, utilisateur introuvable : 401. */
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ApiResponseError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /** Les services utilisent IllegalArgumentException pour une donnée invalide : 400 (et non 500). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** Opération impossible dans l'état actuel (demande déjà traitée...) : 409. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Violation d'une contrainte SQL non anticipée par le service.
     * Le code PostgreSQL (SQLState) permet de donner un message précis :
     * 22001 = valeur trop longue, 23505 = doublon (contrainte UNIQUE), 23503 = référence inexistante,
     * 23502 = champ obligatoire manquant.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        Throwable cause = ex.getMostSpecificCause();
        log.warn("Contrainte SQL violée sur {} : {}", request.getRequestURI(), cause.getMessage());
        String sqlState = cause instanceof SQLException sql ? sql.getSQLState() : null;
        String detail = cause.getMessage() == null ? "" : cause.getMessage();

        if ("22001".equals(sqlState)) {
            return build(HttpStatus.BAD_REQUEST,
                    "Un des champs dépasse la longueur autorisée : raccourcissez le texte saisi", request);
        }
        if ("23502".equals(sqlState)) {
            return build(HttpStatus.BAD_REQUEST, "Un champ obligatoire n'a pas été renseigné", request);
        }
        if ("23503".equals(sqlState)) {
            return build(HttpStatus.CONFLICT,
                    "Cette opération fait référence à un élément qui n'existe pas ou plus", request);
        }
        if ("23505".equals(sqlState)) {
            return build(HttpStatus.CONFLICT, duplicateMessage(detail), request);
        }
        return build(HttpStatus.CONFLICT, "L'opération entre en conflit avec des données existantes", request);
    }

    /** Message lisible pour un doublon, selon la contrainte UNIQUE concernée. */
    private static String duplicateMessage(String detail) {
        String d = detail.toLowerCase();
        if (d.contains("school_name")) return "Ce nom d'auto-école est déjà utilisé";
        if (d.contains("admin_unique") || d.contains("admin_id")) return "Vous avez déjà une demande d'auto-école enregistrée";
        if (d.contains("matriculation")) return "Un véhicule avec cette immatriculation existe déjà";
        if (d.contains("email")) return "Cette adresse email est déjà utilisée";
        if (d.contains("phone")) return "Ce numéro de téléphone est déjà utilisé";
        return "Cet élément existe déjà";
    }

    /**
     * JSON illisible ou paramètre de type incorrect : on précise le champ et la valeur attendue
     * quand c'est possible (ex : date au mauvais format, valeur de liste inconnue, UUID mal formé).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        if (ex.getCause() instanceof InvalidFormatException invalid && !invalid.getPath().isEmpty()) {
            String field = invalid.getPath().get(invalid.getPath().size() - 1).getFieldName();
            Class<?> target = invalid.getTargetType();
            String expected = target.isEnum()
                    ? "valeurs possibles : " + String.join(", ", java.util.Arrays.stream(target.getEnumConstants()).map(Object::toString).toList())
                    : "format attendu : " + target.getSimpleName();
            return build(HttpStatus.BAD_REQUEST,
                    "Valeur invalide pour le champ « " + field + " » (" + expected + ")", request);
        }
        return build(HttpStatus.BAD_REQUEST, "Requête invalide : vérifiez le format des données envoyées", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Valeur invalide pour le paramètre « " + ex.getName() + " »", request);
    }

    /**
     * Toute autre erreur : on la journalise, mais on ne renvoie JAMAIS son message au client
     * (il peut contenir du SQL, des noms de tables, des chemins de fichiers...).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseError> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Erreur inattendue sur {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue", request);
    }

    private ResponseEntity<ApiResponseError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }
}
