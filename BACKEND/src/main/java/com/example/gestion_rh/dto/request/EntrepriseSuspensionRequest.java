package com.example.gestion_rh.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EntrepriseSuspensionRequest {

    @Size(max = 1000)
    private String motif;
}
