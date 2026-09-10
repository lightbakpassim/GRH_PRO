package com.example.gestion_rh.model.converter;

import com.example.gestion_rh.model.Absence;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TypeAbsenceConverter extends SpacedEnumConverter<Absence.TypeAbsence> {
    public TypeAbsenceConverter() {
        super(Absence.TypeAbsence.class);
    }
}
