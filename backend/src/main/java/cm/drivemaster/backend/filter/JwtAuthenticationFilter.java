package cm.drivemaster.backend.filter;


import cm.drivemaster.backend.core.TenantContext;
import cm.drivemaster.backend.services.CustomUserDetailsService;
import cm.drivemaster.backend.services.JwtService;
import cm.drivemaster.backend.services.serviceImpl.PlatformAdminDetailsService;
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

        String token = authHeader.substring(7);
        String tokenType = jwtService.extractClaim(
                token,
                claims -> claims.get("tokenType", String.class)
        );

        String email = jwtService.extractEmail(token);
        String tenant = jwtService.extractTenant(token);
        TenantContext.setTenantId(tenant);

        if (tokenType.equals("PLATFORM_ADMIN")) {

            UserDetails adminDetails =
                    platformAdminDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            adminDetails,
                            null,
                            adminDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            //  Validation SANS entity User
            if (!jwtService.isTokenExpired(token)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
