package com.example.gestion_rh.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "suivi_temps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuiviTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suivi")
    private Integer idSuivi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(name = "date_travail", nullable = false)
    private LocalDate dateTravail;

    @Column(name = "heures_debut", nullable = false)
    private LocalTime heuresDebut;

    @Column(name = "heures_fin", nullable = false)
    private LocalTime heuresFin;

    // Calculé côté BDD via trigger (on envoie 0, le trigger met à jour)
    @Column(name = "heures_travaillees", nullable = false, precision = 5, scale = 2)
    private BigDecimal heuresTravaillees;

    @Column(name = "heures_supplementaires", nullable = false, precision = 5, scale = 2)
    private BigDecimal heuresSupplementaires;

    @Convert(converter = com.example.gestion_rh.model.converter.StatutSuppConverter.class)
    @Column(name = "statut_supp", nullable = false)
    private StatutSupp statutSupp;

    @Column(name = "date_approbation")
    private LocalDateTime dateApprobation;

    public enum StatutSupp {
        Approuvées,
        En_attente,
        Refusées;

        // Correspondance exacte avec le ENUM MySQL qui contient des espaces
        @Override
        public String toString() {
            return this.name().replace("_", " ");
        }
    }
}