package com.transport.reservation.service;

import com.transport.reservation.dto.*;
import com.transport.reservation.entity.Reservation;
import com.transport.reservation.entity.StatutReservation;
import com.transport.reservation.feign.PaiementFeignClient;
import com.transport.reservation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final PaiementFeignClient paiementFeignClient;

    public ReservationService(ReservationRepository reservationRepository,
                              PaiementFeignClient paiementFeignClient) {
        this.reservationRepository = reservationRepository;
        this.paiementFeignClient = paiementFeignClient;
    }

    @Transactional
    public ReservationResponseDTO creerReservation(ReservationRequestDTO dto) {

        Reservation reservation = new Reservation();
        reservation.setClientId(dto.getClientId());
        reservation.setLigneTrajet(dto.getLigneTrajet());
        reservation.setVilleDepart(dto.getVilleDepart());
        reservation.setVilleArrivee(dto.getVilleArrivee());
        reservation.setDateTrajet(dto.getDateTrajet());
        reservation.setHeureDepart(dto.getHeureDepart());
        reservation.setNombrePlaces(dto.getNombrePlaces());
        reservation.setPrixTotal(dto.getPrixTotal());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation creee avec id={}", saved.getId());

        try {
            PaiementRequestDTO paiementRequest = new PaiementRequestDTO(
                    saved.getId(),
                    //saved.getClientId(),
                    saved.getPrixTotal(),
                    "Paiement trajet " + saved.getLigneTrajet()
            );

            PaiementResponseDTO paiementResponse = paiementFeignClient.initierPaiement(paiementRequest);
            log.info("Reponse paiement: statut={}, ref={}", paiementResponse.getStatut(), paiementResponse.getReferencePaiement());

            if ("SUCCES".equals(paiementResponse.getStatut())) {
                saved.setStatut(StatutReservation.CONFIRMEE);
                saved.setReferencePaiement(paiementResponse.getReferencePaiement());
            } else {
                saved.setStatut(StatutReservation.ECHOUEE);
            }

        } catch (Exception e) {
            log.error("Erreur appel service-paiement: {}", e.getMessage());
        }

        Reservation finale = reservationRepository.save(saved);
        return toResponseDTO(finale);
    }

    public ReservationResponseDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation introuvable avec id=" + id));
        return toResponseDTO(reservation);
    }

    public List<ReservationResponseDTO> getReservationsByClient(Long clientId) {
        return reservationRepository.findByClientId(clientId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    public List<ReservationResponseDTO> getToutesLesReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponseDTO annulerReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation introuvable avec id=" + id));

        if (reservation.getStatut() == StatutReservation.CONFIRMEE) {
            throw new RuntimeException("Impossible d'annuler une reservation deja confirmee et payee");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation id={} annulee", id);
        return toResponseDTO(saved);
    }

    private ReservationResponseDTO toResponseDTO(Reservation r) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(r.getId());
        dto.setClientId(r.getClientId());
        dto.setLigneTrajet(r.getLigneTrajet());
        dto.setVilleDepart(r.getVilleDepart());
        dto.setVilleArrivee(r.getVilleArrivee());
        dto.setDateTrajet(r.getDateTrajet());
        dto.setHeureDepart(r.getHeureDepart());
        dto.setNombrePlaces(r.getNombrePlaces());
        dto.setPrixTotal(r.getPrixTotal());
        dto.setStatut(r.getStatut());
        dto.setReferencePaiement(r.getReferencePaiement());
        dto.setDateCreation(r.getDateCreation());
        dto.setDateModification(r.getDateModification());
        return dto;
    }
}
