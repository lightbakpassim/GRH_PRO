package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.Utilisateur;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UtilisateurResponse {
    private Integer idUtilisateur;
    private String login;
    private Utilisateur.StatutUtilisateur statutUtilisateur;
    private LocalDateTime derniereConnexion;
    private Utilisateur.Role role;
    private Integer idEmploye;
    private String nomCompletEmploye;
}