package br.edu.ifsc.gestao_tcc.dto;

import java.time.Instant;
import java.util.Objects;

public record ErrorResponseDTO(
    String timestamp,
    int status,
    String erro,
    String caminho,
    Object detalhes
) {
    public static ErrorResponseDTO of(Instant timestamp, int status, String erro, String caminho, Object detalhes) {
        return new ErrorResponseDTO(
            Objects.requireNonNull(timestamp, "timestamp não pode ser nulo").toString(),
            status,
            erro,
            caminho,
            detalhes
        );
    }
}
