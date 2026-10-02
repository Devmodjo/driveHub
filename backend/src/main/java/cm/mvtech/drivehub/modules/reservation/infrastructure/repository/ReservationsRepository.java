package cm.mvtech.drivehub.modules.reservation.infrastructure.repository;

import cm.mvtech.drivehub.modules.enums.ReservationStatus;
import cm.mvtech.drivehub.modules.reservation.domain.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ReservationsRepository extends JpaRepository<Reservation, UUID> {

    String[] DETAILS = {"student", "student.user", "monitor", "monitor.user", "vehicle"};

    @EntityGraph(attributePaths = {"student", "student.user", "monitor", "monitor.user", "vehicle"})
    Page<Reservation> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user", "monitor", "monitor.user", "vehicle"})
    Page<Reservation> findAllByStudent_Id(UUID studentId, Pageable pageable);

    /** Conflit de planning : le moniteur a-t-il déjà un créneau actif sur cette plage ? */
    boolean existsByMonitor_IdAndDateTimeBetweenAndReservationStatusNot(
            UUID monitorId, LocalDateTime from, LocalDateTime to, ReservationStatus excluded);

    /** Conflit de planning : le véhicule est-il déjà réservé sur cette plage ? */
    boolean existsByVehicle_IdAndDateTimeBetweenAndReservationStatusNot(
            UUID vehicleId, LocalDateTime from, LocalDateTime to, ReservationStatus excluded);
}
