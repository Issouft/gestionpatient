package com.forceN.gestionpatient.securite.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConnexionRequete {

    private String email;
    private String motDePasse;
}
