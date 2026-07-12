package com.example.gestion_rh.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "absence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_absence")
    private Integer idAbsence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(name = "date_absence", nullable = false)
    private LocalDate dateAbsence;

    @Column(name = "motif_absence", nullable = false, columnDefinition = "TEXT")
    private String motifAbsence;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_absence", nullable = false)
    private StatutAbsence statutAbsence;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_absence", nullable = false)
    private TypeAbsence typeAbsence;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_approbation")
    private LocalDateTime dateApprobation;

    public enum StatutAbsence {
        Approuvée,
        En_attente,
        Refusée;

        @Override
        public String toString() {
            return this.name().replace("_", " ");
        }
    }

    public enum TypeAbsence { Maladie, Sans_motif, Retard, Autre;
        @Override
        public String toString() {
            return this.name().replace("_", " ");
        }
    }
}
