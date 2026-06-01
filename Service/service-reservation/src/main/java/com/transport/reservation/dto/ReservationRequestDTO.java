package com.transport.reservation.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservationRequestDTO {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;

    @NotBlank(message = "La ligne de trajet est obligatoire")
    private String ligneTrajet;

    @NotBlank(message = "La ville de depart est obligatoire")
    private String villeDepart;

    @NotBlank(message = "La ville d'arrivee est obligatoire")
    private String villeArrivee;

    @NotNull(message = "La date du trajet est obligatoire")
    @Future(message = "La date du trajet doit etre dans le futur")
    private LocalDate dateTrajet;

    @NotBlank(message = "L'heure de depart est obligatoire")
    private String heureDepart;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Minimum 1 place")
    @Max(value = 10, message = "Maximum 10 places par reservation")
    private Integer nombrePlaces;

    @NotNull(message = "Le prix total est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit etre positif")
    private BigDecimal prixTotal;

    public ReservationRequestDTO() {}

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getLigneTrajet() { return ligneTrajet; }
    public void setLigneTrajet(String ligneTrajet) { this.ligneTrajet = ligneTrajet; }

    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }

    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String villeArrivee) { this.villeArrivee = villeArrivee; }

    public LocalDate getDateTrajet() { return dateTrajet; }
    public void setDateTrajet(LocalDate dateTrajet) { this.dateTrajet = dateTrajet; }

    public String getHeureDepart() { return heureDepart; }
    public void setHeureDepart(String heureDepart) { this.heureDepart = heureDepart; }

    public Integer getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(Integer nombrePlaces) { this.nombrePlaces = nombrePlaces; }

    public BigDecimal getPrixTotal() { return prixTotal; }
    public void setPrixTotal(BigDecimal prixTotal) { this.prixTotal = prixTotal; }
}
