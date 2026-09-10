package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Integer> {

    boolean existsByNomEntrepriseIgnoreCase(String nomEntreprise);

    Optional<Entreprise> findByNomEntrepriseIgnoreCase(String nomEntreprise);
}
