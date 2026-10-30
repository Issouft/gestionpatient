package com.forceN.gestionpatient.entities;

import jakarta.persistence.Entity;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Creneau extends AbstractEntity{

    @CreatedDate
    private Date dateChoisie;
    boolean disponible;
}
