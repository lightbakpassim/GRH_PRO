package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.SuiviTemps;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class SuiviTempsResponse {
    private Integer idSuivi;
    private Integer idEmploye;
    private String nomCompletEmploye;
    private LocalDate dateTravail;
    private LocalTime heuresDebut;
    private LocalTime heuresFin;
    private BigDecimal heuresTravaillees;
    private BigDecimal heuresSupplementaires;
    private SuiviTemps.StatutSupp statutSupp;
    private LocalDateTime dateApprobation;
}
