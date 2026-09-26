package com.example.gestion_rh.dto.response;


import com.example.gestion_rh.model.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String login;
    private Utilisateur.Role role;
    private Integer idEmploye;
    private String nomComplet;
}