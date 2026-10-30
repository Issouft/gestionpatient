package com.forceN.gestionpatient.entities;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Creneau extends AbstractEntity {

    @CreatedDate
    private Date dateChoisie;
    boolean disponible;
}
