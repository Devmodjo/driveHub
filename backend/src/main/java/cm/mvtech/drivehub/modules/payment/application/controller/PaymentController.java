package cm.mvtech.drivehub.modules.payment.application.controller;

import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentRequestDto;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentResponseDto;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentSummaryDto;
import cm.mvtech.drivehub.modules.payment.domain.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Paiements (en-tête X-Tenant-ID obligatoire). */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "PAYMENT API", description = "paiements des élèves (tenant)")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "élève : déclarer un paiement Mobile Money ; moniteur : enregistrer un paiement reçu")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public PaymentResponseDto create(@Valid @RequestBody PaymentRequestDto request) {
        return paymentService.create(request);
    }

    @Operation(summary = "paiements (moniteur : tous ; élève : les siens)")
    @GetMapping
    @PreAuthorize("hasAnyRole('MONITOR','STUDENT')")
    public ApiPageResponse<PaymentResponseDto> list(
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.list(pageable);
    }

    @Operation(summary = "totaux encaissés et en attente")
    @GetMapping("/summary")
    @PreAuthorize("hasRole('MONITOR')")
    public PaymentSummaryDto summary() {
        return paymentService.summary();
    }

    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasRole('MONITOR')")
    public PaymentResponseDto validate(@PathVariable UUID id) {
        return paymentService.validate(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('MONITOR')")
    public PaymentResponseDto reject(@PathVariable UUID id) {
        return paymentService.reject(id);
    }
}
