package cm.mvtech.drivehub.modules.auth.infrastructure.filter;

import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.auth.domain.services.CustomUserDetailsService;
import cm.mvtech.drivehub.modules.auth.domain.services.JwtService;
import cm.mvtech.drivehub.platform.admin.services.serviceImpl.PlatformAdminDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final PlatformAdminDetailsService platformAdminDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            // Extraire les informations du token
            String tokenType = jwtService.extractClaim(
                    token,
                    claims -> claims.get("tokenType", String.class)
            );

            String email = jwtService.extractEmail(token);

            // Vérifier si le token est expiré avant tout traitement
            if (jwtService.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Ne pas modifier le TenantContext ici
            // Le TenantResolutionFilter s'en charge déjà
            // On vérifie juste la cohérence pour les users normaux
            String tokenTenant = jwtService.extractTenant(token);
            String currentTenant = TenantContext.getTenantId();

            if ("PLATFORM_ADMIN".equals(tokenType)) {
                // Pour les admins platform, pas de vérification de tenant
                UserDetails adminDetails = platformAdminDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                adminDetails,
                                null,
                                adminDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                // Pour les users normaux, vérifier la cohérence du tenant
                if (tokenTenant != null && !tokenTenant.equals(currentTenant)
                        && !"public".equals(currentTenant)) {
                    // Incohérence entre le tenant du token et le header
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                            "Tenant mismatch");
                    return;
                }

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer la chaîne
            logger.error("Erreur dans JwtAuthenticationFilter", e);
            filterChain.doFilter(request, response);
        }
    }
}