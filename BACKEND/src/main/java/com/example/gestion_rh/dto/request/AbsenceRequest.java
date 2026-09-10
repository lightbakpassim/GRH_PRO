package com.example.gestion_rh.dto.request;



import com.example.gestion_rh.model.Absence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AbsenceRequest {

    /** Optionnel pour un Employe (forcé côté serveur). */
    private Integer idEmploye;

    @NotNull(message = "La date d'absence est obligatoire")
    private LocalDate dateAbsence;

    @NotBlank(message = "Le motif est obligatoire")
    private String motifAbsence;

    @NotNull(message = "Le type d'absence est obligatoire")
    private Absence.TypeAbsence typeAbsence;
}
