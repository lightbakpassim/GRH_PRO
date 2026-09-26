package com.example.gestion_rh.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Integer idPaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @Min(1) @Max(12)
    @Column(name = "mois")
    private Integer mois;

    @Column(name = "annee", columnDefinition = "YEAR")
    private Integer annee;

    @Column(name = "salaire_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salaireBase;

    @Column(name = "heure_supp_montant", nullable = false, precision = 10, scale = 2)
    private BigDecimal heuresSuppMontant;

    @Column(name = "retenues", nullable = false, precision = 10, scale = 2)
    private BigDecimal retenues;

    // Calculé automatiquement par le trigger BDD
    @Column(name = "total_net", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalNet;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutPaiement statut;

    @OneToMany(mappedBy = "paiement", fetch = FetchType.LAZY)
    private java.util.List<Notification> notifications;

    public enum StatutPaiement {
        En_attente, Effectué;

        @Override
        public String toString() {
            return this.name().replace("_", " ");
        }
    }
}
