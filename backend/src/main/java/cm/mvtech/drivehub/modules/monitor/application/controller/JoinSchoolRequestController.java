package cm.mvtech.drivehub.modules.monitor.application.controller;


import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.messageapi.ApiResponse;
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
@CrossOrigin(originPatterns = "*")
@RequestMapping("/api/join-school")
@RequiredArgsConstructor
public class JoinSchoolRequestController {

    private final SchoolJoinRequestService schoolJoinRequestService;
    private final AdminJoinApprovalService adminJoinApprovalService;
    private final UserRepository userRepository;

    @PostMapping("/public")
    public ResponseEntity<ApiResponse> requestJoin(
            @RequestBody @Valid JoinSchoolRequestDto dto,
            @AuthenticationPrincipal(expression = "username") String email

    ) {
        schoolJoinRequestService.requestJoin(email, dto);
        return ResponseEntity.ok(new ApiResponse(true, "Demande envoyée avec succès"));
    }


    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> approve(
            @PathVariable UUID id
    ) {
        adminJoinApprovalService.approve(id);
        return ResponseEntity.ok(new ApiResponse(true, "Demande approuvée"));
    }


    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
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
