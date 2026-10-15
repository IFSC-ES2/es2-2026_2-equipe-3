package br.edu.ifsc.gestao_tcc.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PrazoEntregaUpdateDTO(
        @NotNull(message = "A data limite é obrigatória.")
        LocalDate dataLimite
) {
}
