package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.Paiement;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaiementResponse {
    private Integer idPaiement;
    private Integer idEmploye;
    private String nomCompletEmploye;
    private Integer mois;
    private Integer annee;
    private BigDecimal salaireBase;
    private BigDecimal heuresSuppMontant;
    private BigDecimal retenues;
    private BigDecimal totalNet;
    private LocalDateTime datePaiement;
    private Paiement.StatutPaiement statut;
}