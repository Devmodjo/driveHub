package cm.mvtech.drivehub.modules.reservation.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.drivingschool.domain.model.DrivingSchool;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.ReservationStatus;
import cm.mvtech.drivehub.modules.enums.ReservationTypes;
import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.enums.State;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.monitor.domain.model.Monitor;
import cm.mvtech.drivehub.modules.monitor.domain.services.MonitorService;
import cm.mvtech.drivehub.modules.reservation.application.dto.ReservationsRequestDto;
import cm.mvtech.drivehub.modules.reservation.application.dto.ReservationsResponseDto;
import cm.mvtech.drivehub.modules.reservation.domain.model.Reservation;
import cm.mvtech.drivehub.modules.reservation.infrastructure.mapper.ReservationsMapper;
import cm.mvtech.drivehub.modules.reservation.infrastructure.repository.ReservationsRepository;
import cm.mvtech.drivehub.modules.student.domain.model.Student;
import cm.mvtech.drivehub.modules.student.domain.services.StudentService;
import cm.mvtech.drivehub.modules.vehicle.domain.model.Vehicle;
import cm.mvtech.drivehub.modules.vehicle.domain.services.VehicleService;
import cm.mvtech.drivehub.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link ReservationService} (planning des leçons).
 *
 * <p>Règles vérifiées :</p>
 * <ul>
 *   <li>un créneau dure 60 minutes : deux créneaux du même moniteur (ou du même véhicule)
 *       qui commencent à moins de 60 minutes d'écart se chevauchent → 409 ;</li>
 *   <li>une leçon de CONDUITE exige un véhicule, et ce véhicule doit être DISPOSABLE ;</li>
 *   <li>un élève ne réserve que pour lui-même (le studentId envoyé est ignoré) → statut PENDING ;
 *       le moniteur réserve pour un élève (studentId obligatoire) → statut CONFIRMED.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationsRepository reservationsRepository;
    @Mock private StudentService studentService;
    @Mock private MonitorService monitorService;
    @Mock private VehicleService vehicleService;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private CurrentSchoolProvider currentSchoolProvider;
    @Spy  private ReservationsMapper mapper = Mappers.getMapper(ReservationsMapper.class);

    @InjectMocks
    private ReservationService service;

    /** Créneau de référence : le 10 janvier 2030 à 9 h. */
    private static final LocalDateTime SLOT = LocalDateTime.of(2030, 1, 10, 9, 0);

    private DrivingSchool school;
    private Student student;
    private Monitor monitor;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        school = TestData.school();
        student = TestData.student(TestData.user(Role.STUDENT));
        monitor = TestData.monitor(TestData.user(Role.MONITOR));
        vehicle = TestData.vehicle("LT 452 AB");
    }

    private ReservationsRequestDto conduite(UUID studentId) {
        return new ReservationsRequestDto(studentId, monitor.getId(), vehicle.getId(), SLOT, ReservationTypes.CONDUITE);
    }

    /** Programme les mocks communs à une réservation faite par l'élève connecté. */
    private void givenStudentIsBooking() {
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(student);
        when(currentSchoolProvider.get()).thenReturn(school);
        when(monitorService.getEntity(monitor.getId())).thenReturn(monitor);
    }

    // ─── create : cas nominaux ────────────────────────────────────────────────

    /**
     * L'élève réserve : la réservation est PENDING et concerne l'élève CONNECTÉ, même si le
     * corps de la requête contient l'identifiant d'un autre élève.
     */
    @Test
    void create_ByStudent_ShouldBePendingAndForHimself() {
        givenStudentIsBooking();
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);
        when(reservationsRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationsResponseDto dto = service.create(conduite(UUID.randomUUID()));

        assertEquals(ReservationStatus.PENDING, dto.reservationStatus());
        assertEquals(student.getId(), dto.studentId());
        assertEquals(monitor.getId(), dto.monitorId());
        assertEquals(vehicle.getId(), dto.vehicleId());
        verify(studentService, never()).getEntity(any());
    }

    /** Le moniteur réserve pour un élève : la réservation est directement CONFIRMED. */
    @Test
    void create_ByMonitor_ShouldBeConfirmed() {
        when(currentUserProvider.isMonitor()).thenReturn(true);
        when(studentService.getEntity(student.getId())).thenReturn(student);
        when(currentSchoolProvider.get()).thenReturn(school);
        when(monitorService.getEntity(monitor.getId())).thenReturn(monitor);
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);
        when(reservationsRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationsResponseDto dto = service.create(conduite(student.getId()));

        assertEquals(ReservationStatus.CONFIRMED, dto.reservationStatus());
        assertEquals(student.getId(), dto.studentId());
    }

    /** Un rendez-vous (hors CONDUITE) peut se faire sans véhicule. */
    @Test
    void create_RendezVousWithoutVehicle_ShouldBeAccepted() {
        givenStudentIsBooking();
        when(reservationsRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationsResponseDto dto = service.create(
                new ReservationsRequestDto(null, monitor.getId(), null, SLOT, ReservationTypes.RENDEZVOUS));

        assertNull(dto.vehicleId());
        verifyNoInteractions(vehicleService);
        verify(reservationsRepository, never())
                .existsByVehicle_IdAndDateTimeBetweenAndReservationStatusNot(any(), any(), any(), any());
    }

    // ─── create : règle des 60 minutes ────────────────────────────────────────

    /**
     * La recherche de conflit couvre [créneau - 59 min ; créneau + 59 min] et ignore les
     * réservations annulées : un créneau à 10 h 00 ne gêne donc pas un créneau à 9 h 00.
     */
    @Test
    void create_ShouldSearchOverlapsWithinSixtyMinutesIgnoringCancelled() {
        givenStudentIsBooking();
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);
        when(reservationsRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(conduite(null));

        verify(reservationsRepository).existsByMonitor_IdAndDateTimeBetweenAndReservationStatusNot(
                monitor.getId(), SLOT.minusMinutes(59), SLOT.plusMinutes(59), ReservationStatus.CANCELLED);
        verify(reservationsRepository).existsByVehicle_IdAndDateTimeBetweenAndReservationStatusNot(
                vehicle.getId(), SLOT.minusMinutes(59), SLOT.plusMinutes(59), ReservationStatus.CANCELLED);
        verify(reservationsRepository).existsByStudent_IdAndDateTimeBetweenAndReservationStatusNot(
                eq(student.getId()), eq(SLOT.minusMinutes(59)), eq(SLOT.plusMinutes(59)), eq(ReservationStatus.CANCELLED));
    }

    /**
     * Non-régression : l'élève n'était pas contrôlé. Il pouvait réserver deux leçons à la même heure
     * avec deux moniteurs et deux véhicules différents.
     */
    @Test
    void create_StudentAlreadyBooked_ShouldThrowConflict() {
        givenStudentIsBooking();
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);
        when(reservationsRepository.existsByStudent_IdAndDateTimeBetweenAndReservationStatusNot(
                eq(student.getId()), any(), any(), eq(ReservationStatus.CANCELLED))).thenReturn(true);

        ConflictException error = assertThrows(ConflictException.class, () -> service.create(conduite(null)));
        assertEquals("L'élève a déjà un créneau à cette heure", error.getMessage());
        verify(reservationsRepository, never()).save(any());
    }

    @Test
    void create_MonitorAlreadyBusy_ShouldThrowConflict() {
        givenStudentIsBooking();
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);
        when(reservationsRepository.existsByMonitor_IdAndDateTimeBetweenAndReservationStatusNot(
                eq(monitor.getId()), any(), any(), eq(ReservationStatus.CANCELLED))).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(conduite(null)));
        verify(reservationsRepository, never()).save(any());
    }

    @Test
    void create_VehicleAlreadyBooked_ShouldThrowConflict() {
        givenStudentIsBooking();
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);
        when(reservationsRepository.existsByVehicle_IdAndDateTimeBetweenAndReservationStatusNot(
                eq(vehicle.getId()), any(), any(), eq(ReservationStatus.CANCELLED))).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(conduite(null)));
        verify(reservationsRepository, never()).save(any());
    }

    // ─── create : véhicule ────────────────────────────────────────────────────

    @Test
    void create_ConduiteWithoutVehicle_ShouldThrowBadRequest() {
        givenStudentIsBooking();

        assertThrows(BadRequestException.class, () -> service.create(
                new ReservationsRequestDto(null, monitor.getId(), null, SLOT, ReservationTypes.CONDUITE)));
        verify(reservationsRepository, never()).save(any());
    }

    /** Un véhicule en panne ou en maintenance ne peut pas être réservé. */
    @Test
    void create_VehicleNotDisposable_ShouldThrowBadRequest() {
        givenStudentIsBooking();
        vehicle.setState(State.PANNE);
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);

        assertThrows(BadRequestException.class, () -> service.create(conduite(null)));

        vehicle.setState(State.MAINTENANCE);
        assertThrows(BadRequestException.class, () -> service.create(conduite(null)));
        verify(reservationsRepository, never()).save(any());
    }

    /** Le moniteur doit préciser pour quel élève il réserve. */
    @Test
    void create_ByMonitorWithoutStudentId_ShouldThrowBadRequest() {
        when(currentUserProvider.isMonitor()).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.create(conduite(null)));
        verifyNoInteractions(reservationsRepository);
    }

    // ─── confirm / cancel ─────────────────────────────────────────────────────

    private Reservation existing(ReservationStatus status) {
        Reservation reservation = new Reservation(school, student, monitor, vehicle, SLOT, ReservationTypes.CONDUITE, status);
        reservation.setId(UUID.randomUUID());
        return reservation;
    }

    @Test
    void confirm_PendingReservation_ShouldBecomeConfirmed() {
        Reservation reservation = existing(ReservationStatus.PENDING);
        when(reservationsRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        ReservationsResponseDto dto = service.confirm(reservation.getId());

        assertEquals(ReservationStatus.CONFIRMED, dto.reservationStatus());
    }

    @Test
    void confirm_NotPendingReservation_ShouldThrowBadRequest() {
        Reservation reservation = existing(ReservationStatus.CANCELLED);
        when(reservationsRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThrows(BadRequestException.class, () -> service.confirm(reservation.getId()));
        assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
    }

    @Test
    void confirm_UnknownReservation_ShouldThrowResourceNotFound() {
        UUID unknown = UUID.randomUUID();
        when(reservationsRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.confirm(unknown));
    }

    @Test
    void cancel_ByMonitor_ShouldBecomeCancelled() {
        Reservation reservation = existing(ReservationStatus.CONFIRMED);
        when(reservationsRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(currentUserProvider.isMonitor()).thenReturn(true);

        assertEquals(ReservationStatus.CANCELLED, service.cancel(reservation.getId()).reservationStatus());
    }

    @Test
    void cancel_ByOwnerStudent_ShouldBecomeCancelled() {
        Reservation reservation = existing(ReservationStatus.PENDING);
        when(reservationsRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(student);

        assertEquals(ReservationStatus.CANCELLED, service.cancel(reservation.getId()).reservationStatus());
    }

    /** Un élève ne peut pas annuler la réservation d'un autre élève. */
    @Test
    void cancel_ByAnotherStudent_ShouldThrowAccessDenied() {
        Reservation reservation = existing(ReservationStatus.PENDING);
        when(reservationsRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(TestData.student(TestData.user(Role.STUDENT)));

        assertThrows(AccessDeniedException.class, () -> service.cancel(reservation.getId()));
        assertEquals(ReservationStatus.PENDING, reservation.getReservationStatus());
    }

    // ─── list ─────────────────────────────────────────────────────────────────

    @Test
    void list_ByMonitor_ShouldReturnAllReservations() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(currentUserProvider.isMonitor()).thenReturn(true);
        when(reservationsRepository.findAllBy(pageable))
                .thenReturn(new PageImpl<>(List.of(existing(ReservationStatus.PENDING)), pageable, 1));

        assertEquals(1, service.list(pageable).totalElements());
        verify(reservationsRepository, never()).findAllByStudent_Id(any(), any());
    }

    @Test
    void list_ByStudent_ShouldReturnOnlyHisReservations() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(currentUserProvider.isMonitor()).thenReturn(false);
        when(studentService.currentStudent()).thenReturn(student);
        when(reservationsRepository.findAllByStudent_Id(student.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(existing(ReservationStatus.PENDING)), pageable, 1));

        assertEquals(1, service.list(pageable).totalElements());
        verify(reservationsRepository, never()).findAllBy(any());
    }

    /** Vérification simple de la capture : la réservation sauvegardée porte l'auto-école du tenant. */
    @Test
    void create_ShouldAttachCurrentSchool() {
        givenStudentIsBooking();
        when(vehicleService.getEntity(vehicle.getId())).thenReturn(vehicle);
        when(reservationsRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(conduite(null));

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationsRepository).save(captor.capture());
        assertSame(school, captor.getValue().getDrivingSchool());
        assertEquals(SLOT, captor.getValue().getDateTime());
    }
}
