package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PrazoEntregaRequestDTO(
        @NotNull(message = "O tipo de entrega é obrigatório.")
        TipoEntrega tipo,

        @NotNull(message = "A data limite é obrigatória.")
        LocalDate dataLimite
) {
}
