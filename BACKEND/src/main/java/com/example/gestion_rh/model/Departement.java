package com.example.gestion_rh.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "departement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private Integer idDepartement;

    @Column(name = "nom_departement", nullable = false, length = 255)
    private String nomDepartement;

    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    private List<Employe> employes;
}
