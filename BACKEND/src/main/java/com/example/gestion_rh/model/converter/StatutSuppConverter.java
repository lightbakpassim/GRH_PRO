package com.example.gestion_rh.model.converter;

import com.example.gestion_rh.model.SuiviTemps;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatutSuppConverter extends SpacedEnumConverter<SuiviTemps.StatutSupp> {
    public StatutSuppConverter() {
        super(SuiviTemps.StatutSupp.class);
    }
}
