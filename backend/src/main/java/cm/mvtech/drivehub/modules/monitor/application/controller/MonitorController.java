package cm.mvtech.drivehub.modules.monitor.application.controller;

import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import cm.mvtech.drivehub.modules.monitor.domain.services.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Moniteurs de l'auto-école (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
@Tag(name = "MONITOR API", description = "moniteurs de l'auto-école (tenant)")
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(summary = "liste des moniteurs (utile à l'élève pour réserver un créneau)")
    @GetMapping
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public List<MonitorResponseDto> list() {
        return monitorService.list();
    }

    @Operation(summary = "profil moniteur de l'utilisateur connecté")
    @GetMapping("/me")
    @PreAuthorize("hasRole('MONITOR')")
    public MonitorResponseDto me() {
        return monitorService.me();
    }
}
