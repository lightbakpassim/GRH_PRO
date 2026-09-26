package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.Notification;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Integer idNotification;
    private Integer idEmploye;
    private Integer idPaiement;
    private String message;
    private Notification.TypeNotification typeNotification;
    private LocalDateTime dateEnvoi;
    private Boolean lu;
}