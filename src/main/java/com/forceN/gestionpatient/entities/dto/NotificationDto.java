package com.forceN.gestionpatient.entities.dto;

import com.forceN.gestionpatient.entities.Utilisateur;
import com.forceN.gestionpatient.entities.enums.TypeNotification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDto {

    private String message;

    private TypeNotification type;

    private LocalDateTime dateEnvoi;

    private boolean lue;
    private Utilisateur destinataire;
    public void marquerCommeLue() {
        this.lue = true;
    }
}
