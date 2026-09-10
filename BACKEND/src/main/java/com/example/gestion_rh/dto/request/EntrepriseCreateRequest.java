package com.example.gestion_rh.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EntrepriseCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String nomEntreprise;

    @Email
    @Size(max = 100)
    private String emailContact;

    @Size(max = 30)
    private String telephone;

    /** Login du compte DG (email pro recommandé). */
    @NotBlank
    @Email
    @Size(max = 50)
    private String dgLogin;

    /** Mot de passe DG optionnel — généré si vide. */
    @Size(min = 8, max = 100)
    private String dgMotDePasse;
}
