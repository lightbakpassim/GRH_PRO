package com.example.gestion_rh.model.converter;

import com.example.gestion_rh.model.Paiement;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatutPaiementConverter extends SpacedEnumConverter<Paiement.StatutPaiement> {
    public StatutPaiementConverter() {
        super(Paiement.StatutPaiement.class);
    }
}
