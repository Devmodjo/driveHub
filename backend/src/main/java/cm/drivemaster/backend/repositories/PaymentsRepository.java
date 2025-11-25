package cm.drivemaster.backend.repositories;


import cm.drivemaster.backend.beans.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {
}
