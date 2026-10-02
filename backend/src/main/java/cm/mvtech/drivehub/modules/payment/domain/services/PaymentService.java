package cm.mvtech.drivehub.modules.payment.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentStatus;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentRequestDto;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentResponseDto;
import cm.mvtech.drivehub.modules.payment.application.dto.PaymentSummaryDto;
import cm.mvtech.drivehub.modules.payment.domain.model.Payment;
import cm.mvtech.drivehub.modules.payment.infrastructure.mapper.PaymentMapper;
import cm.mvtech.drivehub.modules.payment.infrastructure.repository.PaymentsRepository;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.domain.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Paiements des élèves.
 *
 * <ul>
 *   <li>L'élève déclare un paiement Mobile Money (MOMO / OM) : statut PENDING, à valider par le moniteur.</li>
 *   <li>Le moniteur enregistre un paiement reçu (espèces ou Mobile Money vérifié) : statut VALIDATE.</li>
 * </ul>
 * Le statut n'est jamais choisi par le client.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentMapper mapper;
    private final StudentService studentService;
    private final CurrentUserProvider currentUserProvider;
    private final CurrentSchoolProvider currentSchoolProvider;

    @Transactional
    public PaymentResponseDto create(PaymentRequestDto request) {
        boolean byMonitor = currentUserProvider.isMonitor();

        Student student;
        if (byMonitor) {
            if (request.studentsId() == null) {
                throw new BadRequestException("studentsId est obligatoire lorsque le moniteur enregistre un paiement");
            }
            student = studentService.getEntity(request.studentsId());
        } else {
            if (request.method() == PaymentMethod.CASH) {
                throw new BadRequestException("Un paiement en espèces est enregistré par l'auto-école, à la caisse");
            }
            student = studentService.currentStudent();
        }

        Payment payment = new Payment();
        payment.setDrivingSchool(currentSchoolProvider.get());
        payment.setStudent(student);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setMotif(request.motif());
        payment.setPaymentStatus(byMonitor ? PaymentStatus.VALIDATE : PaymentStatus.PENDING);
        return mapper.fromEntityToResponse(paymentsRepository.save(payment));
    }

    /** Moniteur : tous les paiements ; élève : uniquement les siens. */
    @Transactional(readOnly = true)
    public ApiPageResponse<PaymentResponseDto> list(Pageable pageable) {
        var page = currentUserProvider.isMonitor()
                ? paymentsRepository.findAllBy(pageable)
                : paymentsRepository.findAllByStudent_Id(studentService.currentStudent().getId(), pageable);
        return ApiPageResponse.from(page.map(mapper::fromEntityToResponse));
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

    private PaymentResponseDto changePendingStatus(UUID id, PaymentStatus target) {
        Payment payment = paymentsRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + id));
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Ce paiement a déjà été traité");
        }
        payment.setPaymentStatus(target);
        return mapper.fromEntityToResponse(payment);
    }
}
