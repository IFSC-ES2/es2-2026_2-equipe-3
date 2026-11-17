package br.edu.ifsc.gestao_tcc.dto;

import java.time.Instant;

public record ErrorResponseDTO(
    String timestamp,
    int status,
    String erro,
    String caminho,
    Object detalhes
) {
    public static ErrorResponseDTO of(Instant timestamp, int status, String erro, String caminho, Object detalhes) {
        return new ErrorResponseDTO(
            (timestamp != null ? timestamp : Instant.now()).toString(),
            status,
            erro,
            caminho,
            detalhes
        );
    }

    public static ErrorResponseDTO of(int status, String erro, String caminho, Object detalhes) {
        return of(Instant.now(), status, erro, caminho, detalhes);
    }
}
