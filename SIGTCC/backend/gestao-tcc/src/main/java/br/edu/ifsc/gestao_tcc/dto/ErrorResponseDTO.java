package br.edu.ifsc.gestao_tcc.dto;

import java.time.Instant;

public record ErrorResponseDTO(
    String timestamp,
    int status,
    String erro,
    String caminho,
    Object detalhes
) {
    public static ErrorResponseDTO of(int status, String erro, String caminho, Object detalhes) {
        return new ErrorResponseDTO(
            Instant.now().toString(),
            status,
            erro,
            caminho,
            detalhes
        );
    }
}
