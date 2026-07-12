package com.example.gestion_rh.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartementResponse {
    private Integer idDepartement;
    private String nomDepartement;
    private long nombreEmployes;
}