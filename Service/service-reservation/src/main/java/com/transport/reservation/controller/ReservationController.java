package com.transport.reservation.controller;

import com.transport.reservation.dto.ReservationRequestDTO;
import com.transport.reservation.dto.ReservationResponseDTO;
import com.transport.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // POST /api/reservations
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> creerReservation(
            @Valid @RequestBody ReservationRequestDTO dto) {
        ReservationResponseDTO response = reservationService.creerReservation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/reservations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    // GET /api/reservations/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.getReservationsByClient(clientId));
    }

    // GET /api/reservations
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getToutesLesReservations() {
        return ResponseEntity.ok(reservationService.getToutesLesReservations());
    }

    // PUT /api/reservations/{id}/annuler
    @PutMapping("/{id}/annuler")
    public ResponseEntity<ReservationResponseDTO> annulerReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.annulerReservation(id));
    }
}
