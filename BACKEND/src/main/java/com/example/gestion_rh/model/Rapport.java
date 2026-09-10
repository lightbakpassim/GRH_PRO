package com.example.gestion_rh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rapport")
    private Integer idRapport;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_rapport", nullable = false, length = 50)
    private TypeRapport typeRapport;

    @Column(name = "periode_debut", nullable = false)
    private LocalDate periodeDebut;

    @Column(name = "periode_fin", nullable = false)
    private LocalDate periodeFin;

    @Column(name = "date_generation", nullable = false)
    private LocalDateTime dateGeneration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "fichier_pdf", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] fichierPdf;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "lu", nullable = false)
    @Builder.Default
    private Boolean lu = false;

    public enum TypeRapport {
        HEBDOMADAIRE
    }
}
