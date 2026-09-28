package br.edu.ifsc.gestao_tcc.validation;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.SolicitacaoDuplicadaException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoOrientacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidacaoSolicitacaoDuplicada implements ValidacaoSolicitacaoOrientacao {

    private final SolicitacaoOrientacaoRepository solicitacaoRepository;

    @Override
    public void validar(SolicitacaoRequestDTO dto, Aluno aluno, Orientador orientador) {
        boolean existeSolicitacaoPendente = solicitacaoRepository.existsByAlunoIdAndOrientadorIdAndStatus(
                aluno.getId(), orientador.getId(), StatusSolicitacao.PENDENTE);
                
        if (existeSolicitacaoPendente) {
            throw new SolicitacaoDuplicadaException("Você já possui uma solicitação pendente para este orientador.");
        }
    }
}