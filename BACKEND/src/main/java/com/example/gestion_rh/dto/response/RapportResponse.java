package com.example.gestion_rh.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RapportResponse {
    private Integer idRapport;
    private String titre;
    private String typeRapport;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private LocalDateTime dateGeneration;
    private String nomFichier;
    private Boolean lu;
}
