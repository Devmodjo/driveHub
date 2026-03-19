package cm.mvtech.drivehub.core.infrastructure.filter;

import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.core.domain.service.TenantService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TenantResolutionFilter implements Filter {

    private final TenantService tenantService;

    private static final List<String> NO_TENANT_PATHS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/api/auth",
            "/api/driving-schools",
            "/api/platform"
    );

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        log.debug("TenantResolutionFilter - Path: {}", path);

        // Paths qui ne nécessitent pas de tenant (public, auth, platform admin)
        if (NO_TENANT_PATHS.stream().anyMatch(path::startsWith)) {
            TenantContext.setTenantId("public");
            try {
                chain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
            return;
        }

        // Pour les autres endpoints, on a besoin d'un tenant
        String tenantId = extractTenantId(httpRequest);

        if (tenantId == null) {
            log.warn("X-Tenant-ID manquant pour le path: {}", path);
            httpResponse.sendError(
                    HttpStatus.BAD_REQUEST.value(),
                    "X-Tenant-ID manquant"
            );
            return;
        }

        if (!tenantService.isValidTenant(tenantId)) {
            log.warn("Tenant invalide: {} pour le path: {}", tenantId, path);
            httpResponse.sendError(
                    HttpStatus.BAD_REQUEST.value(),
                    "Tenant invalide: " + tenantId
            );
            return;
        }

        try {
            TenantContext.setTenantId(tenantId);
            log.debug("Tenant défini: {}", tenantId);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            log.debug("Tenant context nettoyé");
        }
    }

    private String extractTenantId(HttpServletRequest request) {
        String headerTenant = request.getHeader("X-Tenant-ID");
        if (headerTenant != null && !headerTenant.isBlank()) {
            return headerTenant.trim();
        }
        return null;
    }
}