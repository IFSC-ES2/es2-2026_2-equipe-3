package br.edu.ifsc.gestao_tcc.event;

import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;

public record SolicitacaoStatusChangedEvent(
        SolicitacaoOrientacao solicitacao,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo
) {
}
