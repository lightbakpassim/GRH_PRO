package com.example.gestion_rh.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlateformeDashboardResponse {
    private long totalEntreprises;
    private long entreprisesActives;
    private long entreprisesSuspendues;
    private long totalUtilisateurs;
    private long totalEmployes;
    private long totalPointages7j;
    private long totalConnectes7j;
    private long totalBacklog;
    private List<EntrepriseSanteResponse> entreprises;
    /** Pointages quotidiens (toutes entreprises) */
    private List<SeriePoint> pointages7j;
    /** Actions métier / jour (hors SuperAdmin) */
    private List<SeriePoint> activites7j;
    /** Demandes de congé créées / jour */
    private List<SeriePoint> conges7j;
    private String genereA;

    @Data
    @Builder
    public static class SeriePoint {
        private String label;
        private double valeur;
    }
}
