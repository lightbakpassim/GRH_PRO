package com.example.gestion_rh.dto.request;


import com.example.gestion_rh.model.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotNull(message = "L'id de l'employé est obligatoire")
    private Integer idEmploye;

    private Integer idPaiement;

    @NotBlank(message = "Le message est obligatoire")
    private String message;

    @NotNull(message = "Le type de notification est obligatoire")
    private Notification.TypeNotification typeNotification;
}
