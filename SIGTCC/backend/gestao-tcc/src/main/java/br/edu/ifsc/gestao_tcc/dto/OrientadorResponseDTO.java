package br.edu.ifsc.gestao_tcc.dto;

import java.util.List;

public record OrientadorResponseDTO(
    Long id,
    String nome,
    String email,
    String departamento,
    List<String> linhasDePesquisa,
    Integer vagasDisponiveis,
    String biografia,
    Boolean ativo
) {
}
