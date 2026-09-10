package com.example.gestion_rh.model.converter;

import com.example.gestion_rh.model.Notification;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TypeNotificationConverter extends SpacedEnumConverter<Notification.TypeNotification> {
    public TypeNotificationConverter() {
        super(Notification.TypeNotification.class);
    }
}
