package br.edu.ifsc.gestao_tcc.dto;

import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;

public record AtualizaStatusRequest(
        StatusSolicitacao status,
        String justificativa
) {
}