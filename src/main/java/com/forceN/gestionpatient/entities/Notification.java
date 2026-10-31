package com.forceN.gestionpatient.entities;

import com.forceN.gestionpatient.entities.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Notification extends AbstractEntity {

    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification")
    private TypeNotification type;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    private boolean lue;

    // Utilisateur 1 — * Notification ("reçoit")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    @ToString.Exclude
    private Utilisateur destinataire;

    public void marquerCommeLue() {
        this.lue = true;
    }
}
