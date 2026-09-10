package com.example.gestion_rh.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class EntrepriseDashboardResponse {
    private long employesActifs;
    private long employesInactifs;
    private long totalDepartements;
    private long congesEnAttente;
    private long heuresSuppEnAttente;
    private long pointagesAujourdhui;
    private long paiementsMoisEnCours;
    private long paiementsEnAttente;
    private long paiementsValides;
    private BigDecimal masseSalarialeActifs;
    private long rapportsNonLus;
    private List<DepartementStat> parDepartement;
    private List<ActiviteItem> pointagesRecents;
    private List<ActiviteItem> congesRecents;
    /** 7 derniers jours — pointages / jour */
    private List<SeriePoint> pointages7j;
    /** 7 derniers jours — heures supp cumulées / jour */
    private List<SeriePoint> heuresSupp7j;
    private String genereA;

    @Data
    @Builder
    public static class DepartementStat {
        private Integer idDepartement;
        private String nomDepartement;
        private long effectif;
    }

    @Data
    @Builder
    public static class ActiviteItem {
        private String label;
        private String detail;
        private String statut;
    }

    @Data
    @Builder
    public static class SeriePoint {
        private String label;
        private double valeur;
    }
}
