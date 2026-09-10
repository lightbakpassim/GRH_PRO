package com.example.gestion_rh.dto.response;

import com.example.gestion_rh.model.Entreprise;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EntrepriseSanteResponse {
    private Integer idEntreprise;
    private String nomEntreprise;
    private Entreprise.StatutEntreprise statut;
    private String dgLogin;
    private LocalDateTime dgDerniereConnexion;
    private LocalDateTime derniereActivite;
    private long nbEmployes;
    private long nbEmployesConnectes7j;
    private long nbAdmins;
    private long nbUtilisateursActifs;
    private long pointages7j;
    private long heuresSuppEnAttente;
    private long congesEnAttente;
    private long paiementsEnAttente;
    /** 0–100 */
    private int scoreFlux;
    /** Excellent | Bon | Moyen | Faible | Critique */
    private String qualiteFlux;
}
