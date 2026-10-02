package br.edu.ifsc.gestao_tcc.validation;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.OrientadorInativoException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import org.springframework.stereotype.Component;

@Component
public class ValidacaoOrientadorAtivo implements ValidacaoSolicitacaoOrientacao {

    @Override
    public void validar(SolicitacaoRequestDTO dto, Aluno aluno, Orientador orientador) {
        if (orientador.getAtivo() == null || !orientador.getAtivo()) {
            throw new OrientadorInativoException("O orientador não está disponível para receber solicitações.");
        }
    }
}