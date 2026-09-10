package com.example.gestion_rh.repository;

import com.example.gestion_rh.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByLogin(String login);

    boolean existsByLogin(String login);

    Optional<Utilisateur> findByEmploye_IdEmploye(Integer idEmploye);

    boolean existsByEmploye_IdEmploye(Integer idEmploye);

    List<Utilisateur> findByEntreprise_IdEntreprise(Integer idEntreprise);

    Optional<Utilisateur> findByIdUtilisateurAndEntreprise_IdEntreprise(
            Integer idUtilisateur, Integer idEntreprise);

    @Query("""
            SELECT DISTINCT u FROM Utilisateur u
            LEFT JOIN FETCH u.employe
            WHERE u.entreprise.idEntreprise = :id
            """)
    List<Utilisateur> findByEntrepriseIdWithEmploye(@Param("id") Integer idEntreprise);

    long countByRoleNot(Utilisateur.Role role);

    @Query("""
            SELECT u FROM Utilisateur u
            LEFT JOIN FETCH u.employe
            LEFT JOIN FETCH u.entreprise
            WHERE u.login = :login
            """)
    Optional<Utilisateur> findByLoginWithEmploye(@Param("login") String login);
}
