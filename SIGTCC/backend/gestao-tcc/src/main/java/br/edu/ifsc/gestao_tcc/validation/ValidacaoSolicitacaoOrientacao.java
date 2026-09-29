package br.edu.ifsc.gestao_tcc.validation;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;

public interface ValidacaoSolicitacaoOrientacao {
    void validar(SolicitacaoRequestDTO dto, Aluno aluno, Orientador orientador);
}