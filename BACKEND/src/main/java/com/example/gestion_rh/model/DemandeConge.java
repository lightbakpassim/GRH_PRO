package com.example.gestion_rh.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demande_conge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conge")
    private Integer idConge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "motif_conge", nullable = false, columnDefinition = "TEXT")
    private String motifConge;

    // Calculé automatiquement par le trigger BDD
    @Column(name = "nb_jours", nullable = false)
    private Integer nbJours;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_conge", nullable = false)
    private StatutConge statutConge;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_approbation")
    private LocalDateTime dateApprobation;

    public enum StatutConge {
        Approuvée,
        En_attente,
        Refusée;

        @Override
        public String toString() {
            return this.name().replace("_", " ");
        }
    }
}