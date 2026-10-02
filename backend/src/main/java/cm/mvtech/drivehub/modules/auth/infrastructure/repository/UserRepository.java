package cm.mvtech.drivehub.modules.auth.infrastructure.repository;

import cm.mvtech.drivehub.modules.auth.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Envois groupés du back-office : tous les comptes d'un rôle, sauf un statut (ex : SUSPENDED). */
    java.util.List<User> findAllByRolesAndProfileStatusNot(
            cm.mvtech.drivehub.modules.enums.Role role, cm.mvtech.drivehub.modules.enums.ProfileStatus excluded);

    /** Recherche d'un destinataire par prénom, nom ou email (insensible à la casse). */
    @org.springframework.data.jpa.repository.Query("""
            SELECT u FROM User u
            WHERE lower(u.email) LIKE lower(concat('%', :q, '%'))
               OR lower(u.firstname) LIKE lower(concat('%', :q, '%'))
               OR lower(coalesce(u.lastname, '')) LIKE lower(concat('%', :q, '%'))
            ORDER BY u.firstname
            """)
    java.util.List<User> search(@org.springframework.data.repository.query.Param("q") String q,
                                org.springframework.data.domain.Pageable pageable);
}
