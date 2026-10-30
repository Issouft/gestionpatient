package com.forceN.gestionpatient.entities;

import jakarta.persistence.Entity;
import lombok.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Consultation extends AbstractEntity{
          private String observation;
          private String diagnostic;

}
