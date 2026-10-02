package cm.mvtech.drivehub.modules.payment.infrastructure.repository;

import cm.mvtech.drivehub.modules.enums.PaymentStatus;
import cm.mvtech.drivehub.modules.payment.domain.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PaymentsRepository extends JpaRepository<Payment, UUID> {

    @EntityGraph(attributePaths = {"student", "student.user"})
    Page<Payment> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user"})
    Page<Payment> findAllByStudent_Id(UUID studentId, Pageable pageable);

    /** Total des paiements d'un statut donné (ex : total encaissé = VALIDATE). */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status")
    BigDecimal sumByStatus(@Param("status") PaymentStatus status);
}
