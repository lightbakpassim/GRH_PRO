package com.example.gestion_rh.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaiementCalculResponse {
    private Integer idEmploye;
    private String nomCompletEmploye;
    private Integer mois;
    private Integer annee;
    private BigDecimal salaireBase;
    private BigDecimal heuresSupplementaires;
    private BigDecimal heuresSuppMontant;
    private BigDecimal heuresRetenues;
    private BigDecimal retenues;
    private BigDecimal totalNetEstime;
    private String detailHs;
    private String detailRetenues;
}
