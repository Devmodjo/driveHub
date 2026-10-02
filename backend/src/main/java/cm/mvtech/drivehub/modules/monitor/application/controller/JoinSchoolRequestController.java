package cm.mvtech.drivehub.modules.monitor.application.controller;


import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
import cm.mvtech.drivehub.modules.messageapi.MyJoinRequestResponse;
import cm.mvtech.drivehub.modules.messageapi.JoinSchoolRequestDto;
import cm.mvtech.drivehub.modules.messageapi.PendingJoinRequestResponse;
import cm.mvtech.drivehub.modules.auth.infrastructure.repository.UserRepository;

import cm.mvtech.drivehub.platform.admin.services.serviceImpl.AdminJoinApprovalService;
import cm.mvtech.drivehub.modules.monitor.domain.services.SchoolJoinRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/join-school")
@RequiredArgsConstructor
public class JoinSchoolRequestController {

    private final SchoolJoinRequestService schoolJoinRequestService;
    private final AdminJoinApprovalService adminJoinApprovalService;
    private final UserRepository userRepository;

    /** Mes demandes d'adhésion et leur statut. */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT','MONITOR')")
    public ResponseEntity<java.util.List<MyJoinRequestResponse>> myRequests(
            @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(schoolJoinRequestService.myRequests(email));
    }

    @PostMapping("/public")
    @PreAuthorize("hasAnyRole('STUDENT','MONITOR')")
    public ResponseEntity<ApiResponse> requestJoin(
            @RequestBody @Valid JoinSchoolRequestDto dto,
            @AuthenticationPrincipal(expression = "username") String email

    ) {
        schoolJoinRequestService.requestJoin(email, dto);
        return ResponseEntity.ok(new ApiResponse(true, "Demande envoyée avec succès"));
    }


    /**
     * Correction : hasRole('ADMIN') ne correspondait à aucun rôle existant (MONITOR, STUDENT) :
     * personne ne pouvait approuver. C'est le moniteur responsable de l'auto-école qui valide.
     */
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<ApiResponse> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        adminJoinApprovalService.approve(id, email);
        return ResponseEntity.ok(new ApiResponse(true, "Demande approuvée"));
    }

    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<ApiResponse> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        adminJoinApprovalService.reject(id, email);
        return ResponseEntity.ok(new ApiResponse(true, "Demande rejetée"));
    }


    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<ApiPageResponse<PendingJoinRequestResponse>> pending(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                adminJoinApprovalService.getPendingRequests(email, page, size)
        );
    }

}
