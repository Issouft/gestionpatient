package com.forceN.gestionpatient.rendezvous.dto;

import com.forceN.gestionpatient.rendezvous.enums.TypeNotification;
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
    // Identifiants et DTO seulement : un DTO ne contient jamais d'entité (voir ARCHITECTURE.md).
    private Integer destinataireId;
    public void marquerCommeLue() {
        this.lue = true;
    }
}
