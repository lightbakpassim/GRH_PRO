package com.example.gestion_rh.model.converter;

import com.example.gestion_rh.model.Absence;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatutAbsenceConverter extends SpacedEnumConverter<Absence.StatutAbsence> {
    public StatutAbsenceConverter() {
        super(Absence.StatutAbsence.class);
    }
}
