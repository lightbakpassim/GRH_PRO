package com.example.gestion_rh.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entreprise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entreprise")
    private Integer idEntreprise;

    @Column(name = "nom_entreprise", nullable = false, length = 150)
    private String nomEntreprise;

    @Column(name = "email_contact", length = 100)
    private String emailContact;

    @Column(name = "telephone", length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutEntreprise statut;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_suspension")
    private LocalDateTime dateSuspension;

    @Column(name = "motif_suspension", columnDefinition = "TEXT")
    private String motifSuspension;

    public enum StatutEntreprise {
        Actif, Suspendu
    }
}
