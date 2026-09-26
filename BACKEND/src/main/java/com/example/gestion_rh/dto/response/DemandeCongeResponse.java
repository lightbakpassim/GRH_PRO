package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.DemandeConge;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DemandeCongeResponse {
    private Integer idConge;
    private Integer idEmploye;
    private String nomCompletEmploye;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motifConge;
    private Integer nbJours;
    private DemandeConge.StatutConge statutConge;
    private LocalDateTime dateDemande;
    private LocalDateTime dateApprobation;
}
