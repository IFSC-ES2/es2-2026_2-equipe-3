package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import java.time.Instant;

public record SolicitacaoResponseDTO(
        Long id,
        StatusSolicitacao status,
        String tema,
        String mensagem,
        String justificativa,
        Instant criadoEm,
        AlunoResponse aluno,
        OrientadorResponse orientador
) {
    public record AlunoResponse(
            Long id,
            String nome,
            String email,
            String curso
    ) {
    }

    public record OrientadorResponse(
            Long id,
            String nome,
            String email,
            int vagasDisponiveis
    ) {
    }
}