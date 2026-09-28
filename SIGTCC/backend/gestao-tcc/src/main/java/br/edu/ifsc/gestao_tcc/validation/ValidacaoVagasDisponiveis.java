package br.edu.ifsc.gestao_tcc.validation;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.VagasIndisponiveisException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import org.springframework.stereotype.Component;

@Component
public class ValidacaoVagasDisponiveis implements ValidacaoSolicitacaoOrientacao {

    @Override
    public void validar(SolicitacaoRequestDTO dto, Aluno aluno, Orientador orientador) {
        if (orientador.getPerfil() == null || orientador.getPerfil().getVagasDisponiveis() <= 0) {
            throw new VagasIndisponiveisException("O orientador não possui vagas disponíveis.");
        }
    }
}