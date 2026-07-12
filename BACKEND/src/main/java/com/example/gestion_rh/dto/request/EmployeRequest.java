package com.example.gestion_rh.dto.request;

import com.example.gestion_rh.model.Employe;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nomEmploye;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenomEmploye;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 100)
    private String emailEmploye;

    @Size(max = 20)
    private String telephone;

    @NotNull(message = "Le sexe est obligatoire")
    private Employe.Sexe sexe;

    @NotNull(message = "L'état civil est obligatoire")
    private Employe.EtatCivil etatCivil;

    @NotNull(message = "La date d'embauche est obligatoire")
    private LocalDate dateEmbauche;

    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 100)
    private String poste;

    @NotNull(message = "Le salaire de base est obligatoire")
    @DecimalMin(value = "0.01", message = "Le salaire doit être positif")
    private BigDecimal salaireBase;

    @NotNull(message = "Le statut est obligatoire")
    private Employe.StatutEmploye statutEmploye;

    @NotNull(message = "Le département est obligatoire")
    private Integer idDepartement;
}