package com.transport.reservation.dto;

import java.math.BigDecimal;

public class PaiementRequestDTO {

    private Long reservationId;
    //private Long clientId;
    private BigDecimal montant;
    private String description;

    public PaiementRequestDTO() {}

    public PaiementRequestDTO(Long reservationId, BigDecimal montant, String description) {
        this.reservationId = reservationId;
        //this.clientId = clientId;
        this.montant = montant;
        this.description = description;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    //public Long getClientId() { return clientId; }
    //public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
