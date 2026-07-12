package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.Absence;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AbsenceResponse {
    private Integer idAbsence;
    private Integer idEmploye;
    private String nomCompletEmploye;
    private LocalDate dateAbsence;
    private String motifAbsence;
    private Absence.StatutAbsence statutAbsence;
    private Absence.TypeAbsence typeAbsence;
    private LocalDateTime dateDemande;
    private LocalDateTime dateApprobation;
}