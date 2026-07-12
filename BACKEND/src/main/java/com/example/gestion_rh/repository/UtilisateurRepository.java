package com.example.gestion_rh.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.gestion_rh.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByLogin(String login);

    boolean existsByLogin(String login);

    Optional<Utilisateur> findByEmploye_IdEmploye(Integer idEmploye);

    boolean existsByEmploye_IdEmploye(Integer idEmploye);
    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.employe WHERE u.login = :login")
    Optional<Utilisateur> findByLoginWithEmploye(@Param("login") String login);
}

