package com.transport.reservation.dto;

public class PaiementResponseDTO {

    private String referencePaiement;
    private String statut;
    private String message;

    public PaiementResponseDTO() {}

    public String getReferencePaiement() { return referencePaiement; }
    public void setReferencePaiement(String referencePaiement) { this.referencePaiement = referencePaiement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
