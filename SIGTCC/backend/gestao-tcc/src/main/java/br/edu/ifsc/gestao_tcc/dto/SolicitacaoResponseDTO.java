package br.edu.ifsc.gestao_tcc.dto;

public record SolicitacaoResponseDTO(
    Long id,
    Long orientadorId,
    Long alunoId,
    String tema,
    String mensagem,
    String justificativa,
    String status,
    String criadoEm
) {}