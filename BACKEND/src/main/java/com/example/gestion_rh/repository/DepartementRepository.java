package com.example.gestion_rh.repository;


import com.example.gestion_rh.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Integer> {
    Optional<Departement> findByNomDepartement(String nomDepartement);
    boolean existsByNomDepartement(String nomDepartement);
}
