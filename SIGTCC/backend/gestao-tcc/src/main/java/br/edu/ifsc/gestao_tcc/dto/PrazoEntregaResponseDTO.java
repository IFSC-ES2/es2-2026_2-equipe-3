package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.TipoEntrega;

import java.time.LocalDate;

public record PrazoEntregaResponseDTO(
        Long id,
        String semestre,
        TipoEntrega tipo,
        String descricao,
        LocalDate dataLimite
) {
}
