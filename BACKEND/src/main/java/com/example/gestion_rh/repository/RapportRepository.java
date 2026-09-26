package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.Rapport;
import com.example.gestion_rh.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, Integer> {

    List<Rapport> findByDestinataireOrderByDateGenerationDesc(Utilisateur destinataire);

    long countByDestinataireAndLuFalse(Utilisateur destinataire);

    List<Rapport> findAllByOrderByDateGenerationDesc();

    long countByLuFalse();
}
