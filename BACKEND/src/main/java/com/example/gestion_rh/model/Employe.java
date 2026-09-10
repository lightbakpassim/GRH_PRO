package com.example.gestion_rh.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employe")
    private Integer idEmploye;

    @Column(name = "nom_employe", nullable = false, length = 100)
    private String nomEmploye;

    @Column(name = "prenom_employe", nullable = false, length = 100)
    private String prenomEmploye;

    @Email
    @Column(name = "email_employe", nullable = false, unique = true, length = 100)
    private String emailEmploye;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexe", nullable = false)
    private Sexe sexe;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_civil", nullable = false)
    private EtatCivil etatCivil;

    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;

    @Column(name = "poste", nullable = false, length = 100)
    private String poste;

    @DecimalMin(value = "0.01", message = "Le salaire de base doit être positif")
    @Column(name = "salaire_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salaireBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_employe", nullable = false)
    private StatutEmploye statutEmploye;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departement")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;

    @OneToOne(mappedBy = "employe", fetch = FetchType.LAZY)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    private List<SuiviTemps> suiviTemps;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    private List<DemandeConge> demandesConge;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    private List<Absence> absences;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    private List<Paiement> paiements;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    private List<Notification> notifications;

    // Enums internes
    public enum Sexe { M, F }

    public enum EtatCivil {
        Célibataire, Marié, Divorcé, Veuf
    }

    public enum StatutEmploye { Actif, Inactif }
}
