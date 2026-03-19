package cm.drivemaster.backend.controllers;


import cm.drivemaster.backend.models.dto.ApiPageResponse;
import cm.drivemaster.backend.models.dto.ApiResponse;
import cm.drivemaster.backend.models.dto.JoinSchoolRequestDto;
import cm.drivemaster.backend.models.dto.PendingJoinRequestResponse;
import cm.drivemaster.backend.repositories.UserRepository;

import cm.drivemaster.backend.services.serviceImpl.AdminJoinApprovalService;
import cm.drivemaster.backend.services.serviceImpl.SchoolJoinRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



@RestController
@CrossOrigin(originPatterns = "*")
@RequestMapping("/api/join-school")
@RequiredArgsConstructor
public class JoinRequestController {

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
            @PathVariable Long id
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
