package cm.drivemaster.backend.core;

import cm.drivemaster.backend.services.TenantService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.io.IOException;


@RequiredArgsConstructor
public class TenantResolutionFilter implements Filter {

    private final TenantService tenantService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/api/auth"
    );

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // ROUTES PUBLIQUES → PAS DE TENANT
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            TenantContext.setTenantId("public");
            chain.doFilter(request, response);
            TenantContext.clear();
            return;
        }

        String tenantId = extractTenantId(httpRequest);

        if (tenantId == null || !tenantService.isValidTenant(tenantId)) {
            ((HttpServletResponse) response)
                    .sendError(HttpStatus.BAD_REQUEST.value(),
                            "Tenant invalide ou manquant");
            return;
        }

        try {
            TenantContext.setTenantId(tenantId);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractTenantId(HttpServletRequest request) {

        String headerTenant = request.getHeader("X-Tenant-ID");
        if (headerTenant != null && !headerTenant.isBlank()) {
            return headerTenant;
        }

        String host = request.getServerName();
        if (host.contains(".")) {
            return host.split("\\.")[0];
        }
        return null;
    }
}
