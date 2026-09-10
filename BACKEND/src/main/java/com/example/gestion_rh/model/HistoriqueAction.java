package com.example.gestion_rh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Integer idHistorique;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "acteur_login", length = 50)
    private String acteurLogin;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;
}
