package cm.mvtech.drivehub.modules.exception;

import cm.mvtech.drivehub.modules.messageapi.ApiResponseError;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Traduction des erreurs SQL en messages compréhensibles (sans base de données :
 * l'exception PostgreSQL est simulée avec son code SQLState).
 */
class GlobalHandlerExceptionTest {

    private final GlobalHandlerException handler = new GlobalHandlerException();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");

    private ResponseEntity<ApiResponseError> handle(String sqlState, String detail) {
        return handler.handleDataIntegrity(
                new DataIntegrityViolationException("erreur", new SQLException(detail, sqlState)), request);
    }

    /** Cas du bug signalé : valeur trop longue (22001) → 400 lisible, et non une erreur technique. */
    @Test
    void valueTooLong_Returns400WithReadableMessage() {
        ResponseEntity<ApiResponseError> response =
                handle("22001", "ERROR: value too long for type character varying(255)");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Un des champs dépasse la longueur autorisée : raccourcissez le texte saisi",
                response.getBody().message());
    }

    /** Doublon (23505) : le message dépend de la contrainte concernée. */
    @Test
    void uniqueViolation_ReturnsSpecificDuplicateMessage() {
        ResponseEntity<ApiResponseError> response = handle("23505",
                "duplicate key value violates unique constraint \"driving_school_registry_school_name_unique\"");

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ce nom d'auto-école est déjà utilisé", response.getBody().message());
    }

    @Test
    void uniqueViolationOnEmail_ReturnsEmailMessage() {
        assertEquals("Cette adresse email est déjà utilisée",
                handle("23505", "duplicate key (email)=(a@b.c)").getBody().message());
    }

    @Test
    void notNullViolation_Returns400() {
        ResponseEntity<ApiResponseError> response = handle("23502", "null value in column \"address\"");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Un champ obligatoire n'a pas été renseigné", response.getBody().message());
    }

    @Test
    void foreignKeyViolation_Returns409() {
        assertEquals(HttpStatus.CONFLICT, handle("23503", "violates foreign key constraint").getStatusCode());
    }

    @Test
    void unknownAddress_Is404_AndWrongMethodIs405() {
        var notFound = handler.handleNoResource(
                new org.springframework.web.servlet.resource.NoResourceFoundException(
                        org.springframework.http.HttpMethod.GET, "v3/api-docs"), request);
        assertEquals(404, notFound.getStatusCode().value());

        var wrongMethod = handler.handleMethodNotSupported(
                new org.springframework.web.HttpRequestMethodNotSupportedException("GET"), request);
        assertEquals(405, wrongMethod.getStatusCode().value());
        assertTrue(wrongMethod.getBody().message().contains("GET"));
    }
}
