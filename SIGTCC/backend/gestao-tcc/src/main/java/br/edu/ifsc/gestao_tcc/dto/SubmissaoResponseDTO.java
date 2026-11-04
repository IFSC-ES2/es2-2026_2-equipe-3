package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.Submissao;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;

import java.time.Instant;

public record SubmissaoResponseDTO(
        Long id,
        Long alunoId,
        TipoEntrega tipo,
        String urlDocumento,
        Instant enviadoEm
) {
    public static SubmissaoResponseDTO fromEntity(Submissao submissao) {
        return new SubmissaoResponseDTO(
                submissao.getId(),
                submissao.getAluno().getId(),
                submissao.getTipo(),
                submissao.getUrlDocumento(),
                submissao.getEnviadoEm()
        );
    }
}
