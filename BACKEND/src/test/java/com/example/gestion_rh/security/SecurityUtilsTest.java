package com.example.gestion_rh.security;

import com.example.gestion_rh.exception.BusinessException;
import com.example.gestion_rh.model.Employe;
import com.example.gestion_rh.model.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private Utilisateur admin() {
        return Utilisateur.builder()
                .idUtilisateur(1)
                .login("admin")
                .role(Utilisateur.Role.Admin)
                .build();
    }

    private Utilisateur employe(int idEmploye) {
        Employe e = Employe.builder().idEmploye(idEmploye).build();
        return Utilisateur.builder()
                .idUtilisateur(2)
                .login("emp")
                .role(Utilisateur.Role.Employe)
                .employe(e)
                .build();
    }

    @Test
    void adminPeutCiblerNimporteQuelEmploye() {
        assertEquals(99, SecurityUtils.resolveTargetEmployeId(admin(), 99));
    }

    @Test
    void adminSansIdEmployeLeveBusinessException() {
        assertThrows(BusinessException.class,
                () -> SecurityUtils.resolveTargetEmployeId(admin(), null));
    }

    @Test
    void employeEstForceSurSonPropreId() {
        assertEquals(5, SecurityUtils.resolveTargetEmployeId(employe(5), 99));
    }

    @Test
    void assertOwnsRefuseAutreEmploye() {
        assertThrows(AccessDeniedException.class,
                () -> SecurityUtils.assertOwnsEmployeResource(employe(5), 7));
    }

    @Test
    void assertOwnsAccepteProprietaireOuAdmin() {
        assertDoesNotThrow(() -> SecurityUtils.assertOwnsEmployeResource(employe(5), 5));
        assertDoesNotThrow(() -> SecurityUtils.assertOwnsEmployeResource(admin(), 7));
    }

    @Test
    void requireIdEmployeSansLienLeveBusinessException() {
        Utilisateur u = Utilisateur.builder()
                .idUtilisateur(3)
                .login("orphan")
                .role(Utilisateur.Role.Employe)
                .build();
        assertThrows(BusinessException.class, () -> SecurityUtils.requireIdEmploye(u));
    }
}
