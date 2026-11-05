package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.StatusEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;

import java.time.Instant;

public record EntregaResponseDTO(
        Long id,
        String semestre,
        TipoEntrega tipo,
        String descricaoTipo,
        String urlDocumento,
        StatusEntrega status,
        Boolean atrasada,
        Instant enviadoEm,
        AlunoResponse aluno,
        OrientadorResponse orientador,
        ParecerResumoResponse parecer
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
            String email
    ) {
    }

    public record ParecerResumoResponse(
            Long id,
            StatusEntrega resultado,
            String comentario,
            Instant registradoEm
    ) {
    }
}
