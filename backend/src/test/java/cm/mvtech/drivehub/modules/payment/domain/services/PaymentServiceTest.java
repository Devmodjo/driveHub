package cm.mvtech.drivehub.modules.payment.domain.services;

import cm.mvtech.drivehub.core.infrastructure.TenantContext;
import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.PaymentMethod;
import cm.mvtech.drivehub.modules.enums.PaymentMotif;
import cm.mvtech.drivehub.modules.enums.PaymentProvider;
import cm.mvtech.drivehub.modules.enums.PaymentStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
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
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link PaymentService}.
 *
 * <ul>
 *   <li>Moniteur : enregistre un paiement reçu (espèces) → provider MANUAL, statut VALIDATE.</li>
 *   <li>Élève : paie par Mobile Money via la passerelle active → statut PENDING, puis
 *       « Vérifier » (refresh) → VALIDATE ou REJECTED selon la réponse de la passerelle.</li>
 *   <li>Validation / rejet manuel : uniquement pour un paiement encore PENDING.</li>
 * </ul>
 *
 * <p>La passerelle de paiement est un mock : aucun appel réseau n'est fait.</p>
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String TENANT = "ae_ecole_test_abc123";

    @Mock private PaymentsRepository paymentsRepository;
    @Mock private StudentService studentService;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private CurrentSchoolProvider currentSchoolProvider;
    @Mock private PaymentGatewayResolver gatewayResolver;
    @Mock private PaymentGateway gateway;
    @Spy  private PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @InjectMocks
    private PaymentService service;

    private DrivingSchool school;
    private Student student;

    @BeforeEach
    void setUp() {
        school = TestData.school();
        student = TestData.student(TestData.user(Role.STUDENT));
        // Le service lit le tenant courant pour construire la référence "schema:idPaiement"
        TenantContext.setTenantId(TENANT);
    }

    @AfterEach
    void tearDown() {
        // Toujours nettoyer un ThreadLocal : sinon il « fuit » vers le test suivant
        TenantContext.clear();
    }

    /** Simule la base : save() attribue un identifiant, comme le ferait Hibernate. */
    private void givenSaveAssignsId() {
        when(paymentsRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment payment = inv.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(UUID.randomUUID());
            }
            return payment;
        });
    }

    private Payment pendingMobilePayment() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setStudent(student);
        payment.setDrivingSchool(school);
        payment.setAmount(new BigDecimal("50000"));
        payment.setMethod(PaymentMethod.MOMO);
        payment.setMotif(PaymentMotif.INSCRIPTION);
        payment.setProvider(PaymentProvider.SIMULATED);
        payment.setExternalReference("SIM-237677112233-1");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        return payment;
    }

    // ─── create : moniteur ────────────────────────────────────────────────────

    /** Paiement en espèces saisi par le moniteur : validé immédiatement, sans passerelle. */
    @Test
    void create_ByMonitorCash_ShouldBeManualAndValidated() {
        when(currentUserProvider.isMonitor()).thenReturn(true);
        when(studentService.getEntity(student.getId())).thenReturn(student);
        when(currentSchoolProvider.get()).thenReturn(school);
        givenSaveAssignsId();

        PaymentResponseDto dto = service.create(new PaymentRequestDto(
                student.getId(), new BigDecimal("25000"), PaymentMethod.CASH, PaymentMotif.INSCRIPTION, null));

        assertEquals(PaymentStatus.VALIDATE, dto.paymentStatus());
        assertEquals(PaymentProvider.MANUAL, dto.provider());
        assertEquals(student.getId(), dto.studentId());
        assertEquals(0, new BigDecimal("25000").compareTo(dto.amount()));
        verifyNoInteractions(gatewayResolver);
    }

    @Test
    void create_ByMonitorWithoutStudent_ShouldThrowBadRequest() {
        when(currentUserProvider.isMonitor()).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.create(new PaymentRequestDto(
                null, new BigDecimal("25000"), PaymentMethod.CASH, PaymentMotif.INSCRIPTION, null)));
        verifyNoInteractions(paymentsRepository);
    }

    // ─── create : élève (Mobile Money) ────────────────────────────────────────

    /**
     * Paiement MoMo de l'élève : enregistré PENDING, puis la passerelle est appelée avec la
     * référence interne "schema:idPaiement" ; la référence externe et le message sont conservés.
     */
    @Test
    void create_ByStudentMomo_ShouldCallGatewayAndStayPending() {
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(student);
        when(gatewayResolver.active()).thenReturn(gateway);
        when(gateway.provider()).thenReturn(PaymentProvider.SIMULATED);
        when(currentSchoolProvider.get()).thenReturn(school);
        givenSaveAssignsId();
        when(gateway.initiate(any(GatewayRequest.class)))
                .thenReturn(new GatewayResult(GatewayResult.Status.PENDING, "SIM-REF-1", "Validez sur votre téléphone"));

        PaymentResponseDto dto = service.create(new PaymentRequestDto(
                null, new BigDecimal("50000"), PaymentMethod.MOMO, PaymentMotif.INSCRIPTION, " +237 677 11 22 33 "));

        ArgumentCaptor<GatewayRequest> captor = ArgumentCaptor.forClass(GatewayRequest.class);
        verify(gateway).initiate(captor.capture());
        assertEquals(TENANT + ":" + dto.id(), captor.getValue().internalReference());
        assertEquals("+237 677 11 22 33", captor.getValue().phoneNumber());

        assertEquals(PaymentStatus.PENDING, dto.paymentStatus());
        assertEquals(PaymentProvider.SIMULATED, dto.provider());
        assertEquals("SIM-REF-1", dto.externalReference());
        assertEquals("Validez sur votre téléphone", dto.gatewayMessage());
        assertEquals(student.getId(), dto.studentId());
    }

    /** Orange Money : même parcours ; si la passerelle refuse tout de suite, le paiement est REJECTED. */
    @Test
    void create_ByStudentOm_GatewayFailure_ShouldBeRejected() {
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(student);
        when(gatewayResolver.active()).thenReturn(gateway);
        when(gateway.provider()).thenReturn(PaymentProvider.SIMULATED);
        when(currentSchoolProvider.get()).thenReturn(school);
        givenSaveAssignsId();
        when(gateway.initiate(any(GatewayRequest.class))).thenReturn(GatewayResult.failed("Numéro inconnu"));

        PaymentResponseDto dto = service.create(new PaymentRequestDto(
                null, new BigDecimal("10000"), PaymentMethod.OM, PaymentMotif.EXAMS, "+237699000001"));

        assertEquals(PaymentStatus.REJECTED, dto.paymentStatus());
        assertEquals("Numéro inconnu", dto.gatewayMessage());
    }

    /** Un élève ne peut pas déclarer un paiement en espèces : c'est l'auto-école qui l'enregistre. */
    @Test
    void create_ByStudentCash_ShouldThrowBadRequest() {
        when(currentUserProvider.isMonitor()).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.create(new PaymentRequestDto(
                null, new BigDecimal("1000"), PaymentMethod.CASH, PaymentMotif.EXAMS, "+237677112233")));
        verifyNoInteractions(paymentsRepository, gatewayResolver);
    }

    @Test
    void create_ByStudentWithoutPhone_ShouldThrowBadRequest() {
        when(currentUserProvider.isMonitor()).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.create(new PaymentRequestDto(
                null, new BigDecimal("1000"), PaymentMethod.MOMO, PaymentMotif.EXAMS, "   ")));
        verifyNoInteractions(paymentsRepository, gatewayResolver);
    }

    // ─── refresh ──────────────────────────────────────────────────────────────

    /** « Vérifier » : la passerelle confirme → VALIDATE. */
    @Test
    void refresh_PendingPaymentConfirmedByGateway_ShouldBecomeValidated() {
        Payment payment = pendingMobilePayment();
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(student);
        when(gatewayResolver.get(PaymentProvider.SIMULATED)).thenReturn(gateway);
        when(gateway.checkStatus(payment.getExternalReference()))
                .thenReturn(new GatewayResult(GatewayResult.Status.SUCCESSFUL, payment.getExternalReference(), null));

        PaymentResponseDto dto = service.refresh(payment.getId());

        assertEquals(PaymentStatus.VALIDATE, dto.paymentStatus());
    }

    /** « Vérifier » : la passerelle signale un échec → REJECTED. */
    @Test
    void refresh_PendingPaymentFailedAtGateway_ShouldBecomeRejected() {
        Payment payment = pendingMobilePayment();
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(currentUserProvider.isMonitor()).thenReturn(true);
        when(gatewayResolver.get(PaymentProvider.SIMULATED)).thenReturn(gateway);
        when(gateway.checkStatus(payment.getExternalReference()))
                .thenReturn(new GatewayResult(GatewayResult.Status.FAILED, payment.getExternalReference(), "Solde insuffisant"));

        assertEquals(PaymentStatus.REJECTED, service.refresh(payment.getId()).paymentStatus());
    }

    /** Un paiement déjà traité (ou MANUAL) n'est pas renvoyé à la passerelle. */
    @Test
    void refresh_AlreadyValidatedPayment_ShouldNotCallGateway() {
        Payment payment = pendingMobilePayment();
        payment.setPaymentStatus(PaymentStatus.VALIDATE);
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(currentUserProvider.isMonitor()).thenReturn(true);

        assertEquals(PaymentStatus.VALIDATE, service.refresh(payment.getId()).paymentStatus());
        verifyNoInteractions(gatewayResolver);
    }

    /** Un élève ne peut pas vérifier le paiement d'un autre élève. */
    @Test
    void refresh_PaymentOfAnotherStudent_ShouldThrowAccessDenied() {
        Payment payment = pendingMobilePayment();
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(TestData.student(TestData.user(Role.STUDENT)));

        assertThrows(AccessDeniedException.class, () -> service.refresh(payment.getId()));
        verifyNoInteractions(gatewayResolver);
    }

    @Test
    void refresh_UnknownPayment_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(paymentsRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.refresh(unknown));
    }

    // ─── validate / reject ────────────────────────────────────────────────────

    @Test
    void validate_PendingPayment_ShouldBecomeValidated() {
        Payment payment = pendingMobilePayment();
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertEquals(PaymentStatus.VALIDATE, service.validate(payment.getId()).paymentStatus());
    }

    @Test
    void reject_PendingPayment_ShouldBecomeRejected() {
        Payment payment = pendingMobilePayment();
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertEquals(PaymentStatus.REJECTED, service.reject(payment.getId()).paymentStatus());
    }

    /** Seul un paiement PENDING peut être validé ou rejeté à la main. */
    @Test
    void validateOrReject_AlreadyProcessedPayment_ShouldThrowBadRequest() {
        Payment payment = pendingMobilePayment();
        payment.setPaymentStatus(PaymentStatus.REJECTED);
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> service.validate(payment.getId()));
        assertThrows(BadRequestException.class, () -> service.reject(payment.getId()));
        assertEquals(PaymentStatus.REJECTED, payment.getPaymentStatus());
    }

    // ─── applyGatewayNotification (webhook) ───────────────────────────────────

    @Test
    void applyGatewayNotification_MatchingPendingPayment_ShouldApplyResult() {
        Payment payment = pendingMobilePayment();
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.applyGatewayNotification(payment.getId(), payment.getExternalReference(),
                new GatewayResult(GatewayResult.Status.SUCCESSFUL, payment.getExternalReference(), null));

        assertEquals(PaymentStatus.VALIDATE, payment.getPaymentStatus());
    }

    /** Une référence externe qui ne correspond pas au paiement est ignorée (notification suspecte). */
    @Test
    void applyGatewayNotification_WrongReference_ShouldBeIgnored() {
        Payment payment = pendingMobilePayment();
        when(paymentsRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.applyGatewayNotification(payment.getId(), "AUTRE-REF",
                new GatewayResult(GatewayResult.Status.SUCCESSFUL, "AUTRE-REF", null));

        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
    }

    // ─── summary ──────────────────────────────────────────────────────────────

    @Test
    void summary_ShouldReturnTotalsByStatus() {
        when(paymentsRepository.sumByStatus(PaymentStatus.VALIDATE)).thenReturn(new BigDecimal("75000"));
        when(paymentsRepository.sumByStatus(PaymentStatus.PENDING)).thenReturn(new BigDecimal("10000"));

        PaymentSummaryDto summary = service.summary();

        assertEquals(new BigDecimal("75000"), summary.totalValidated());
        assertEquals(new BigDecimal("10000"), summary.totalPending());
    }
}
