package cm.drivemaster.backend.core;

import cm.drivemaster.backend.services.TenantService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@Order(1)
@RequiredArgsConstructor
public class TenantResolutionFilter implements Filter {

    private final TenantService tenantService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = extractTenantId(httpRequest);

        // Validate tenant exists and is active
        if (!tenantService.isValidTenant(tenantId)) {
            ((HttpServletResponse) response).setStatus(HttpStatus.FORBIDDEN.value());
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
        // Strategy 1: Subdomain extraction
        String host = request.getServerName();
        if (host.contains(".")) {
            return host.split("\\.")[0];
        }

        // Strategy 2: Header-based
        String headerTenant = request.getHeader("X-Tenant-ID");
        if (headerTenant != null) {
            return headerTenant;
        }

        // Strategy 3: Path-based
        String path = request.getRequestURI();
        if (path.startsWith("/tenant/")) {
            return path.split("/")[2];
        }

        return "default";
    }
}
