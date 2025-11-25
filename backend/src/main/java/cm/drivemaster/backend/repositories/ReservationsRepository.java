package cm.drivemaster.backend.repositories;

import cm.drivemaster.backend.beans.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationsRepository extends JpaRepository<Reservations, Long> {
}
