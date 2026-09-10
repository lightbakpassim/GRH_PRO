package com.example.gestion_rh.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Java Enum.name() (underscores) ↔ valeurs MariaDB avec espaces. */
@Converter
public class SpacedEnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected SpacedEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        if (attribute == null) return null;
        return attribute.name().replace('_', ' ');
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return Enum.valueOf(enumClass, dbData.trim().replace(' ', '_'));
    }
}
