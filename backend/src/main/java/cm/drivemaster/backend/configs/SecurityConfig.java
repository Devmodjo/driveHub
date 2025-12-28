package cm.drivemaster.backend.configs;


import cm.drivemaster.backend.core.TenantResolutionFilter;
import cm.drivemaster.backend.filter.JwtAuthenticationFilter;
import cm.drivemaster.backend.services.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

        TenantResolutionFilter tenantFilter =
                new TenantResolutionFilter(tenantService);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())

                // Ordre SIMPLE et STABLE
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/auth/**",
                                "/api/driving-schools/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}
