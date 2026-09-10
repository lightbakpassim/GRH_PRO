package com.example.gestion_rh.dto.request;


import com.example.gestion_rh.model.Utilisateur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UtilisateurRequest {

    @NotBlank(message = "Le login est obligatoire")
    @Size(min = 3, max = 50, message = "Le login doit avoir entre 3 et 50 caractères")
    private String login;

    /** Optionnel à la création : un mot de passe unique est généré et envoyé par email. */
    @Size(min = 6, message = "Le mot de passe doit avoir au moins 6 caractères")
    private String motDePasse;

    @NotNull(message = "Le statut est obligatoire")
    private Utilisateur.StatutUtilisateur statutUtilisateur;

    @NotNull(message = "Le rôle est obligatoire")
    private Utilisateur.Role role;

    /** Obligatoire sauf pour le rôle DG. */
    private Integer idEmploye;
}
