package br.edu.ifsc.gestao_tcc.dto;

public record PerfilProfessorResponseDTO(
    Long id,
    String nome,
    String email,
    String departamento,
    Boolean responsavelTurma
) {
}
