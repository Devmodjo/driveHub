package cm.mvtech.drivehub.configs;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation Swagger : http://localhost:8082/swagger-ui.html
 *
 * <p>Le bouton « Authorize » propose deux champs, envoyés ensuite avec chaque requête :</p>
 * <ul>
 *   <li><b>bearerAuth</b> : le jeton reçu à la connexion (sans le mot « Bearer ») ;</li>
 *   <li><b>tenantId</b> : le schéma de l'auto-école (en-tête X-Tenant-ID), obligatoire pour les routes
 *       métier (/api/students, /api/vehicles, /api/reservations...). Sa valeur est le claim « tenant »
 *       du jeton (visible sur jwt.io) ou la colonne schema_name de driving_school_registry.
 *       Laisser vide pour les routes publiques (/api/auth, /api/platform, /api/driving-schools, /api/join-school).</li>
 * </ul>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Drivehub – API SaaS Gestion des Auto-Écoles",
                version = "1.0",
                description = "API multitenant (PostgreSQL – Separate Schema). "
                        + "Guide de test pas à pas : docs/GUIDE-TEST-SWAGGER.md"
        ),
        security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "tenantId")}
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT obtenu via /api/auth/login (moniteur, élève) ou /api/platform/admin/login (admin)"
)
@SecurityScheme(
        name = "tenantId",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-Tenant-ID",
        description = "Schéma de l'auto-école (claim « tenant » du jeton). Uniquement pour les routes métier."
)
public class OpenApiConfig {
}
