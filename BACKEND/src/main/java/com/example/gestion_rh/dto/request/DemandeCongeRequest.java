package com.example.gestion_rh.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DemandeCongeRequest {

    @NotNull(message = "L'id de l'employé est obligatoire")
    private Integer idEmploye;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @NotBlank(message = "Le motif est obligatoire")
    private String motifConge;
}