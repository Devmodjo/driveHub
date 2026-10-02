package cm.mvtech.drivehub.core.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantSchemasTest {

    @Test
    void generatesSafeUniqueSchemaNameFromSchoolName() {
        String schema = TenantSchemas.fromSchoolName("Auto-École Le Volant !");
        assertTrue(schema.matches("^ae_auto_ecole_le_volant_[a-f0-9]{6}$"), schema);
        assertNotEquals(schema, TenantSchemas.fromSchoolName("Auto-École Le Volant !"), "suffixe aléatoire");
    }

    @Test
    void rejectsSqlInjectionAttempts() {
        assertFalse(TenantSchemas.isValid("public; DROP SCHEMA public"));
        assertFalse(TenantSchemas.isValid("1ecole"));
        assertFalse(TenantSchemas.isValid(null));
        assertThrows(IllegalArgumentException.class, () -> TenantSchemas.requireValid("x\"; --"));
        assertTrue(TenantSchemas.isValid("ae_ecole_1a2b3c"));
    }
}
