package com.example.gestion_rh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfilRequest {

    /** Nouveau login (email). Optionnel si seul le MDP change. */
    @Size(min = 3, max = 50, message = "Login entre 3 et 50 caractères")
    private String login;

    @NotBlank(message = "Le mot de passe actuel est obligatoire pour modifier le compte")
    private String motDePasseActuel;

    /** Nouveau mot de passe — optionnel. */
    @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
    private String nouveauMotDePasse;
}
