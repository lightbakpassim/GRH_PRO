package com.example.gestion_rh.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HistoriqueActionResponse {
    private Integer idHistorique;
    private String action;
    private String detail;
    private String acteurLogin;
    private LocalDateTime dateAction;
}
