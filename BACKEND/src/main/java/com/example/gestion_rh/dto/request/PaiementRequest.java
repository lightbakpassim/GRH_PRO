package com.example.gestion_rh.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaiementRequest {

    @NotNull(message = "L'id de l'employé est obligatoire")
    private Integer idEmploye;

    @NotNull @Min(1) @Max(12)
    private Integer mois;

    @NotNull
    private Integer annee;

    @NotNull @DecimalMin("0.00")
    private BigDecimal salaireBase;

    @NotNull @DecimalMin("0.00")
    private BigDecimal heuresSuppMontant;

    @NotNull @DecimalMin("0.00")
    private BigDecimal retenues;
}
