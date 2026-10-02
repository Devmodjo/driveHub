package cm.mvtech.drivehub.modules.reservation.domain.services;

import cm.mvtech.drivehub.modules.auth.domain.services.CurrentUserProvider;
import cm.mvtech.drivehub.modules.drivingschool.domain.services.CurrentSchoolProvider;
import cm.mvtech.drivehub.modules.enums.ReservationStatus;
import cm.mvtech.drivehub.modules.enums.ReservationTypes;
import cm.mvtech.drivehub.modules.enums.State;
import cm.mvtech.drivehub.modules.exception.BadRequestException;
import cm.mvtech.drivehub.modules.exception.ConflictException;
import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import cm.mvtech.drivehub.modules.messageapi.ApiPageResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Réservation de créneaux (leçons de conduite, rendez-vous).
 *
 * <p>Règles : un créneau dure {@value #SLOT_MINUTES} minutes ; un moniteur ou un véhicule ne peut
 * pas avoir deux créneaux qui se chevauchent ; une leçon de CONDUITE exige un véhicule disponible.
 * Une réservation faite par l'élève est PENDING (à confirmer), celle faite par le moniteur est CONFIRMED.</p>
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    static final int SLOT_MINUTES = 60;

    private final ReservationsRepository reservationsRepository;
    private final ReservationsMapper mapper;
    private final StudentService studentService;
    private final MonitorService monitorService;
    private final VehicleService vehicleService;
    private final CurrentUserProvider currentUserProvider;
    private final CurrentSchoolProvider currentSchoolProvider;

    @Transactional
    public ReservationsResponseDto create(ReservationsRequestDto request) {
        boolean byMonitor = currentUserProvider.isMonitor();

        Student student;
        if (byMonitor) {
            if (request.studentId() == null) {
                throw new BadRequestException("studentId est obligatoire lorsque le moniteur réserve");
            }
            student = studentService.getEntity(request.studentId());
        } else {
            student = studentService.currentStudent();   // un élève ne réserve que pour lui-même
        }

        Reservation reservation = new Reservation();
        reservation.setDrivingSchool(currentSchoolProvider.get());
        reservation.setStudent(student);
        reservation.setMonitor(monitorService.getEntity(request.monitorId()));
        reservation.setVehicle(resolveVehicle(request));
        reservation.setDateTime(request.dateTime());
        reservation.setTypes(request.types());
        reservation.setReservationStatus(byMonitor ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING);

        checkNoOverlap(reservation);
        return mapper.fromEntityToResponse(reservationsRepository.save(reservation));
    }

    /** Moniteur : toutes les réservations ; élève : uniquement les siennes. */
    @Transactional(readOnly = true)
    public ApiPageResponse<ReservationsResponseDto> list(Pageable pageable) {
        var page = currentUserProvider.isMonitor()
                ? reservationsRepository.findAllBy(pageable)
                : reservationsRepository.findAllByStudent_Id(studentService.currentStudent().getId(), pageable);
        return ApiPageResponse.from(page.map(mapper::fromEntityToResponse));
    }

    @Transactional
    public ReservationsResponseDto confirm(UUID id) {
        Reservation reservation = getEntity(id);
        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Seule une réservation en attente peut être confirmée");
        }
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        return mapper.fromEntityToResponse(reservation);
    }

    /** Annulation : par le moniteur, ou par l'élève concerné. */
    @Transactional
    public ReservationsResponseDto cancel(UUID id) {
        Reservation reservation = getEntity(id);
        if (!currentUserProvider.isMonitor()
                && !reservation.getStudent().getId().equals(studentService.currentStudent().getId())) {
            throw new AccessDeniedException("Vous ne pouvez annuler que vos propres réservations");
        }
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        return mapper.fromEntityToResponse(reservation);
    }

    private Vehicle resolveVehicle(ReservationsRequestDto request) {
        if (request.vehicleId() == null) {
            if (request.types() == ReservationTypes.CONDUITE) {
                throw new BadRequestException("Un véhicule est obligatoire pour une leçon de conduite");
            }
            return null;
        }
        Vehicle vehicle = vehicleService.getEntity(request.vehicleId());
        if (vehicle.getState() != State.DISPOSABLE) {
            throw new BadRequestException("Le véhicule " + vehicle.getMatriculation() + " n'est pas disponible");
        }
        return vehicle;
    }

    /** Deux créneaux se chevauchent s'ils commencent à moins de SLOT_MINUTES l'un de l'autre. */
    private void checkNoOverlap(Reservation reservation) {
        LocalDateTime from = reservation.getDateTime().minusMinutes(SLOT_MINUTES - 1);
        LocalDateTime to = reservation.getDateTime().plusMinutes(SLOT_MINUTES - 1);

        if (reservationsRepository.existsByMonitor_IdAndDateTimeBetweenAndReservationStatusNot(
                reservation.getMonitor().getId(), from, to, ReservationStatus.CANCELLED)) {
            throw new ConflictException("Le moniteur a déjà un créneau à cette heure");
        }
        if (reservation.getVehicle() != null
                && reservationsRepository.existsByVehicle_IdAndDateTimeBetweenAndReservationStatusNot(
                reservation.getVehicle().getId(), from, to, ReservationStatus.CANCELLED)) {
            throw new ConflictException("Le véhicule est déjà réservé à cette heure");
        }
    }

    private Reservation getEntity(UUID id) {
        return reservationsRepository.findById(id)
                .filter(reservation -> !reservation.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + id));
    }
}
