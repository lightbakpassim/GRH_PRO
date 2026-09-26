package com.example.gestion_rh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Integer idNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paiement")
    private Paiement paiement;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification", nullable = false)
    private TypeNotification typeNotification;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "lu")
    private Boolean lu;

    public enum TypeNotification {
        Paie, Congés, Avertissement, Erreur, Heure_supplémentaire;

        @Override
        public String toString() {
            return this.name().replace("_", " ");
        }
    }
}
