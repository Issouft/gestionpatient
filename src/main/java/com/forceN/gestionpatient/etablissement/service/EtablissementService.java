package com.forceN.gestionpatient.etablissement.service;

import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Les établissements de la plateforme, gérés par le super-administrateur. */
@Service
@RequiredArgsConstructor
public class EtablissementService {

    private final EtablissementRepository etablissementRepository;
}
