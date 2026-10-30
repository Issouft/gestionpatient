package com.forceN.gestionpatient.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Notifications extends AbstractEntity{

    @Column(name = "message")
    private String message;

    @Column(name = "Type_notification")
    private String typeNotification;

    @Column(name = "Date_envoi")
    private Date dateEnvoi;

    boolean lue;
}
