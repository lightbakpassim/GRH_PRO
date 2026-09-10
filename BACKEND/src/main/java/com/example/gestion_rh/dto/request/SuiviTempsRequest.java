package com.example.gestion_rh.dto.request;


import com.example.gestion_rh.model.SuiviTemps;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SuiviTempsRequest {

    /** Optionnel pour un Employe (forcé côté serveur). */
    private Integer idEmploye;

    @NotNull(message = "La date de travail est obligatoire")
    private LocalDate dateTravail;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heuresDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heuresFin;

    private SuiviTemps.StatutSupp statutSupp = SuiviTemps.StatutSupp.En_attente;
}