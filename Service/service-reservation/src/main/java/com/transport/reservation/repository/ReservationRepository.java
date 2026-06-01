package com.transport.reservation.repository;

import com.transport.reservation.entity.Reservation;
import com.transport.reservation.entity.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Toutes les réservations d'un client
    List<Reservation> findByClientId(Long clientId);

    // Réservations d'un client filtrées par statut
    List<Reservation> findByClientIdAndStatut(Long clientId, StatutReservation statut);

    // Toutes les réservations par statut
    List<Reservation> findByStatut(StatutReservation statut);
}
