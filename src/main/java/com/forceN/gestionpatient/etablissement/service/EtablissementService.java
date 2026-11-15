package com.forceN.gestionpatient.etablissement.service;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Les établissements de la plateforme. Leur création par le super-administrateur viendra ici. */
@Service
@RequiredArgsConstructor
public class EtablissementService {

    // Même nom que dans la migration V3, qui rattache les comptes d'avant le SaaS.
    static final String CLINIQUE_PRINCIPALE = "Clinique principale";

    private final EtablissementRepository etablissementRepository;

    /**
     * L'établissement du premier administrateur : le plus ancien, ou une « Clinique principale » créée
     * s'il n'en existe aucun (base neuve). Solution d'attente : le super-administrateur créera les établissements.
     */
    @Transactional
    public Integer etablissementParDefaut() {
        return etablissementRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> etablissementRepository.save(Etablissement.builder()
                        .nom(CLINIQUE_PRINCIPALE)
                        .type(TypeEtablissement.CLINIQUE)
                        .build()))
                .getId();
    }
}
