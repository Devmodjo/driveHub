package cm.mvtech.drivehub.modules.monitor.application.controller;

import cm.mvtech.drivehub.modules.monitor.application.dto.AddMonitorRequest;
import cm.mvtech.drivehub.modules.monitor.application.dto.MonitorResponseDto;
import cm.mvtech.drivehub.modules.monitor.domain.services.MonitorInvitationService;
import cm.mvtech.drivehub.modules.monitor.domain.services.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Moniteurs de l'auto-école (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
@Tag(name = "MONITOR API", description = "moniteurs de l'auto-école (tenant)")
public class MonitorController {

    private final MonitorService monitorService;
    private final MonitorInvitationService invitationService;

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

    @Operation(summary = "Ajouter un moniteur (responsable de l'auto-école)",
            description = "multipart/form-data : partie « monitor » (JSON, application/json), fichiers « cni » et « capec » "
                    + "(PDF, JPEG, PNG ou WebP, 5 Mo max), « cniNumber » et « capecNumber » facultatifs. "
                    + "Le moniteur reçoit un email pour choisir son mot de passe (lien valable 72 h).")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<MonitorResponseDto> add(@Valid @RequestPart("monitor") AddMonitorRequest monitor,
                                                  @RequestPart("cni") MultipartFile cni,
                                                  @RequestPart("capec") MultipartFile capec,
                                                  @RequestParam(value = "cniNumber", required = false) String cniNumber,
                                                  @RequestParam(value = "capecNumber", required = false) String capecNumber) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.addMonitor(monitor, cni, cniNumber, capec, capecNumber));
    }
}
