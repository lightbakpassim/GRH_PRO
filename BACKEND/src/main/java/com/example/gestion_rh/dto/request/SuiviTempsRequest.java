package com.example.gestion_rh.dto.request;


import com.example.gestion_rh.model.SuiviTemps;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SuiviTempsRequest {

    @NotNull(message = "L'id de l'employé est obligatoire")
    private Integer idEmploye;

    @NotNull(message = "La date de travail est obligatoire")
    private LocalDate dateTravail;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heuresDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heuresFin;

    // Statut initial des heures supp, défaut "En attente"
    private SuiviTemps.StatutSupp statutSupp = SuiviTemps.StatutSupp.En_attente;
}