package cm.mvtech.drivehub.modules.payment.domain.services;

import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.enums.PaymentStatus;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentRequestDto;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentResponseDto;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentSummaryDto;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayRequest;
import cm.mvtech.drivehub.modules.payment.domain.gateway.GatewayResult;
import cm.mvtech.drivehub.modules.payment.domain.gateway.PaymentGateway;
import cm.mvtech.drivehub.modules.payment.domain.model.Payment;
import cm.mvtech.drivehub.modules.payment.infrastructure.gateway.PaymentGatewayResolver;
import cm.mvtech.drivehub.modules.payment.infrastructure.mapper.PaymentMapper;
import cm.mvtech.drivehub.modules.payment.infrastructure.repository.PaymentsRepository;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.domain.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Paiements des élèves.
 *
 * <ul>
 *   <li><b>Élève</b> : paiement Mobile Money (MTN MoMo / Orange Money) via la passerelle active
 *       (Campay, ou simulation en développement). Le paiement est PENDING jusqu'à la validation sur le
 *       téléphone, puis passe VALIDATE ou REJECTED (webhook Campay ou bouton "Vérifier").</li>
 *   <li><b>Moniteur</b> : enregistre un paiement reçu à l'auto-école (espèces...) → VALIDATE ;
 *       peut aussi valider / rejeter à la main un paiement en attente.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentMapper mapper;
    private final StudentService studentService;
    private final CurrentUserProvider currentUserProvider;
    private final CurrentSchoolProvider currentSchoolProvider;
    private final PaymentGatewayResolver gatewayResolver;

    @Transactional
    public PaymentResponseDto create(PaymentRequestDto request) {
        return currentUserProvider.isMonitor() ? recordManualPayment(request) : payByMobileMoney(request);
    }

    /** Paiement reçu à l'auto-école et saisi par le moniteur. */
    private PaymentResponseDto recordManualPayment(PaymentRequestDto request) {
        if (request.studentsId() == null) {
            throw new BadRequestException("studentsId est obligatoire lorsque le moniteur enregistre un paiement");
        }
        Payment payment = newPayment(studentService.getEntity(request.studentsId()), request);
        payment.setProvider(PaymentProvider.MANUAL);
        payment.setPaymentStatus(PaymentStatus.VALIDATE);
        return mapper.fromEntityToResponse(paymentsRepository.save(payment));
    }

    /** Paiement en ligne de l'élève connecté. */
    private PaymentResponseDto payByMobileMoney(PaymentRequestDto request) {
        if (request.method() == PaymentMethod.CASH) {
            throw new BadRequestException("Un paiement en espèces est enregistré par l'auto-école, à la caisse");
        }
        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            throw new BadRequestException("Le numéro Mobile Money est obligatoire");
        }
        Student student = studentService.currentStudent();
        PaymentGateway gateway = gatewayResolver.active();

        // 1. On enregistre d'abord le paiement (PENDING) pour obtenir son identifiant.
        Payment payment = newPayment(student, request);
        payment.setProvider(gateway.provider());
        payment.setPhoneNumber(request.phoneNumber().trim());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentsRepository.save(payment);

        // 2. Demande de débit. La référence interne "schema:id" permet au webhook de retrouver l'auto-école.
        GatewayResult result = gateway.initiate(new GatewayRequest(
                TenantContext.getTenantId() + ":" + payment.getId(),
                payment.getAmount(),
                payment.getPhoneNumber(),
                "DriveHub - " + request.motif() + " - " + student.getUser().getFirstname()));
        apply(payment, result);
        return mapper.fromEntityToResponse(payment);
    }

    /** Moniteur : tous les paiements ; élève : uniquement les siens. */
    @Transactional(readOnly = true)
    public ApiPageResponse<PaymentResponseDto> list(Pageable pageable) {
        var page = currentUserProvider.isMonitor()
                ? paymentsRepository.findAllBy(pageable)
                : paymentsRepository.findAllByStudent_Id(studentService.currentStudent().getId(), pageable);
        return ApiPageResponse.from(page.map(mapper::fromEntityToResponse));
    }

    /** Ré-interroge la passerelle pour un paiement en attente ("Vérifier" dans l'interface). */
    @Transactional
    public PaymentResponseDto refresh(UUID id) {
        Payment payment = getEntity(id);
        if (!currentUserProvider.isMonitor()
                && !payment.getStudent().getId().equals(studentService.currentStudent().getId())) {
            throw new AccessDeniedException("Ce paiement ne vous appartient pas");
        }
        if (payment.getPaymentStatus() == PaymentStatus.PENDING && payment.getExternalReference() != null
                && payment.getProvider() != PaymentProvider.MANUAL) {
            apply(payment, gatewayResolver.get(payment.getProvider()).checkStatus(payment.getExternalReference()));
        }
        return mapper.fromEntityToResponse(payment);
    }

    /**
     * Notification de l'agrégateur (webhook). Doit être appelée DANS le schéma de l'auto-école
     * (voir PaymentWebhookService). Ignore les références inconnues ou déjà traitées.
     */
    @Transactional
    public void applyGatewayNotification(UUID paymentId, String externalReference, GatewayResult result) {
        paymentsRepository.findById(paymentId)
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .filter(p -> externalReference == null || externalReference.equals(p.getExternalReference()))
                .ifPresent(payment -> apply(payment, result));
    }

    @Transactional
    public PaymentResponseDto validate(UUID id) {
        return changePendingStatus(id, PaymentStatus.VALIDATE);
    }

    @Transactional
    public PaymentResponseDto reject(UUID id) {
        return changePendingStatus(id, PaymentStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public PaymentSummaryDto summary() {
        return new PaymentSummaryDto(
                paymentsRepository.sumByStatus(PaymentStatus.VALIDATE),
                paymentsRepository.sumByStatus(PaymentStatus.PENDING));
    }

    // ------------------------------------------------------------------ privé

    private Payment newPayment(Student student, PaymentRequestDto request) {
        Payment payment = new Payment();
        payment.setDrivingSchool(currentSchoolProvider.get());
        payment.setStudent(student);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setMotif(request.motif());
        return payment;
    }

    /** Reporte la réponse de la passerelle sur le paiement (sauvegarde en fin de transaction). */
    private void apply(Payment payment, GatewayResult result) {
        if (result.externalReference() != null) {
            payment.setExternalReference(result.externalReference());
        }
        payment.setGatewayMessage(result.message());
        payment.setPaymentStatus(switch (result.status()) {
            case SUCCESSFUL -> PaymentStatus.VALIDATE;
            case FAILED -> PaymentStatus.REJECTED;
            case PENDING -> PaymentStatus.PENDING;
        });
    }

    private PaymentResponseDto changePendingStatus(UUID id, PaymentStatus target) {
        Payment payment = getEntity(id);
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Ce paiement a déjà été traité");
        }
        payment.setPaymentStatus(target);
        return mapper.fromEntityToResponse(payment);
    }

    private Payment getEntity(UUID id) {
        return paymentsRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + id));
    }
}
