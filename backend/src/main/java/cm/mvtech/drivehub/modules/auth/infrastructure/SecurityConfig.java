package cm.mvtech.drivehub.modules.auth.infrastructure;

import cm.mvtech.drivehub.core.infrastructure.filter.TenantResolutionFilter;
import cm.mvtech.drivehub.modules.auth.infrastructure.filter.JwtAuthenticationFilter;
import cm.mvtech.drivehub.core.domain.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
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
                .cors(cors -> cors.disable())

                // IMPORTANT: Session stateless pour éviter les problèmes
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Ordre des filtres: TENANT, JWT
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, TenantResolutionFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics (pas d'auth requise)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/auth/**",              // Login/Register users normaux
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
                                "/api/auth/reset-password"
                        ).permitAll()

                        // Endpoints admin platform (authentifié)
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