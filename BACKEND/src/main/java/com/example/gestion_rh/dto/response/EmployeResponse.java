package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.Employe;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EmployeResponse {
    private Integer idEmploye;
    private String nomEmploye;
    private String prenomEmploye;
    private String emailEmploye;
    private String telephone;
    private Employe.Sexe sexe;
    private Employe.EtatCivil etatCivil;
    private LocalDate dateEmbauche;
    private String poste;
    private BigDecimal salaireBase;
    private Employe.StatutEmploye statutEmploye;
    private Integer idDepartement;
    private String nomDepartement;
    /** Présents uniquement à la création. */
    private Boolean compteCree;
    private Boolean emailEnvoye;
    private String motDePasseTemporaire;
    private String avertissement;
}
