package com.forceN.gestionpatient.etablissement.repository;

import com.forceN.gestionpatient.etablissement.entities.Etablissement;
import com.forceN.gestionpatient.etablissement.enums.TypeEtablissement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EtablissementRepository extends JpaRepository<Etablissement, Integer> {

    /**
     * Les établissements actifs, triés par nom. Un filtre à null est ignoré.
     * La ville doit être donnée en minuscules : la comparaison ignore ainsi les majuscules.
     */
    @Query("""
            select e from Etablissement e
            where e.actif = true
              and (:type is null or e.type = :type)
              and (:villeMinuscules is null or lower(e.ville) = :villeMinuscules)
            order by e.nom, e.id
            """)
    List<Etablissement> trouverActifs(TypeEtablissement type, String villeMinuscules);

    Optional<Etablissement> findByIdAndActifTrue(Integer id);
}
