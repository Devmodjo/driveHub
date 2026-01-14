package cm.drivemaster.backend.configs;

import cm.drivemaster.backend.core.TenantResolutionFilter;
import cm.drivemaster.backend.filter.JwtAuthenticationFilter;
import cm.drivemaster.backend.services.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
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

                // Ordre des filtres: TENANT d'abord, puis JWT
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

                        // Endpoints admin platform (authentifié)
                        .requestMatchers("/api/platform/**").authenticated()

                        // Endpoints tenant-specific (authentifié + tenant requis)
                        .requestMatchers("/api/join-school/admin/**").authenticated()
                        .requestMatchers("/api/join-school/public").authenticated()

                        // Tout le reste nécessite authentification
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}