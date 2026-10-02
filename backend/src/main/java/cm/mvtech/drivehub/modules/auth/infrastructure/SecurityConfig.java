package cm.mvtech.drivehub.modules.auth.infrastructure;

import cm.mvtech.drivehub.core.infrastructure.filter.TenantResolutionFilter;
import cm.mvtech.drivehub.modules.auth.infrastructure.filter.JwtAuthenticationFilter;
import cm.mvtech.drivehub.core.domain.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantService tenantService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        TenantResolutionFilter tenantFilter = new TenantResolutionFilter(tenantService);

        http
                .csrf(csrf -> csrf.disable())
                // CORS : règles définies dans configs/CorsConfig (origines lues dans application.yaml)
                .cors(Customizer.withDefaults())

                // IMPORTANT: Session stateless pour éviter les problèmes
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Route protégée appelée sans jeton valide (absent, expiré, révoqué) : 401 et non 403
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // Ordre des filtres: TENANT, JWT
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, TenantResolutionFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics (pas d'auth requise)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/error",                    // page d'erreur Spring : sinon un 401/400 envoyé par un filtre devient un 403 vide
                                "/api/auth/**",              // Login/Register users normaux
                                "/api/webhooks/**",          // Webhooks Campay (vérifiés par signature)
                                "/api/driving-schools/**",   // Liste publique des écoles
                                "/api/platform/admin/login", // Login admin platform
                                "/api/platform/admin/register" // Register admin platform
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register/**",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/accept-invitation"
                        ).permitAll()
                        .requestMatchers(
                                "/api/platform/admin/login",
                                "/api/platform/admin/register",
                                "/api/platform/admin/verify-email",
                                "/api/platform/admin/resend-verification",
                                "/api/platform/admin/forgot-password",
                                "/api/platform/admin/reset-password"
                        ).permitAll()

                        // Endpoints admin platform (authentifié)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/platform/**").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()
                        // Endpoints tenant-specific (authentifié + tenant requis)
                        .requestMatchers("/api/join-school/admin/**").authenticated()
                        .requestMatchers("/api/join-school/public").authenticated()

                        // Tout le reste nécessite authentification
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}