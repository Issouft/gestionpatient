package com.forceN.gestionpatient.etablissement;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import com.forceN.gestionpatient.etablissement.repository.EtablissementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** Vérifie que l'entité Etablissement correspond à la table créée par la migration V2. */
@SpringBootTest
@Transactional // chaque test est annulé à la fin : la base n'est pas modifiée
class EtablissementTest {

    @Autowired
    private EtablissementRepository etablissementRepository;

    @Test
    void unEtablissementEstEnregistreEtActifParDefaut() {
        Etablissement clinique = etablissementRepository.saveAndFlush(Etablissement.builder()
                .nom("Clinique Test")
                .type(TypeEtablissement.CLINIQUE)
                .ville("Thiès")
                .build());

        Etablissement relue = etablissementRepository.findById(clinique.getId()).orElseThrow();
        assertThat(relue.getNom()).isEqualTo("Clinique Test");
        assertThat(relue.getType()).isEqualTo(TypeEtablissement.CLINIQUE);
        assertThat(relue.isActif()).isTrue();
        assertThat(relue.getCreationDate()).isNotNull();
    }
}
