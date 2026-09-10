package com.example.gestion_rh.model.converter;

import com.example.gestion_rh.model.DemandeConge;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatutCongeConverter extends SpacedEnumConverter<DemandeConge.StatutConge> {
    public StatutCongeConverter() {
        super(DemandeConge.StatutConge.class);
    }
}
