package com.example.gestion_rh.dto.response;

import com.example.gestion_rh.model.Entreprise;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EntrepriseResponse {
    private Integer idEntreprise;
    private String nomEntreprise;
    private String emailContact;
    private String telephone;
    private Entreprise.StatutEntreprise statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateSuspension;
    private String motifSuspension;
    private String dgLogin;
    private LocalDateTime dgDerniereConnexion;
    private long nbEmployes;
    private long nbAdmins;
    private long nbUtilisateursActifs;
    /** Mot de passe DG en clair uniquement à la création / reset. */
    private String dgMotDePasseTemporaire;
    private String rhLogin;
    /** Mot de passe RH en clair uniquement à la création. */
    private String rhMotDePasseTemporaire;
}
