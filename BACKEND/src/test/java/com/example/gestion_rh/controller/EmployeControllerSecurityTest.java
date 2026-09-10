package com.example.gestion_rh.controller;

import com.example.gestion_rh.dto.response.PageResponse;
import com.example.gestion_rh.repository.UtilisateurRepository;
import com.example.gestion_rh.security.JwtAuthentificationFilter;
import com.example.gestion_rh.security.JwtService;
import com.example.gestion_rh.service.EmployeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmployeController.class)
@AutoConfigureMockMvc
@Import(EmployeControllerSecurityTest.TestSecurityConfig.class)
class EmployeControllerSecurityTest {

    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/employes/**").hasRole("Admin")
                            .anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeService employeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthentificationFilter jwtAuthentificationFilter;

    @MockBean
    private UtilisateurRepository utilisateurRepository;

    @Test
    void sansAuthRefuse() throws Exception {
        mockMvc.perform(get("/api/employes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "Employe")
    void roleEmployeRefuseListe() throws Exception {
        mockMvc.perform(get("/api/employes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Admin")
    void roleAdminAutoriseListe() throws Exception {
        when(employeService.findAll(any())).thenReturn(PageResponse.ofList(List.of()));
        mockMvc.perform(get("/api/employes"))
                .andExpect(status().isOk());
    }
}
