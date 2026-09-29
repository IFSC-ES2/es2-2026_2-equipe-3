package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoOrientacaoRepository;
import br.edu.ifsc.gestao_tcc.validation.ValidacaoSolicitacaoOrientacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoOrientacaoService {

    private final SolicitacaoOrientacaoRepository solicitacaoRepository;
    private final OrientadorRepository orientadorRepository;
    private final AlunoRepository alunoRepository;
    private final List<ValidacaoSolicitacaoOrientacao> validacoes;

    @Transactional
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO dto) {
        Orientador orientador = orientadorRepository.findById(dto.getOrientadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Orientador com identificador " + dto.getOrientadorId() + " não foi encontrado."));

        Aluno aluno = alunoRepository.findByEmail(dto.getAluno().getEmail())
                .orElseGet(() -> {
                    Aluno novoAluno = Aluno.builder()
                            .nome(dto.getAluno().getNome())
                            .email(dto.getAluno().getEmail())
                            .curso(dto.getAluno().getCurso())
                            .build();
                    return alunoRepository.save(novoAluno);
                });

        validacoes.forEach(validacao -> validacao.validar(dto, aluno, orientador));

        SolicitacaoOrientacao solicitacao = SolicitacaoOrientacao.builder()
                .orientador(orientador)
                .aluno(aluno)
                .tema(dto.getTema())
                .mensagem(dto.getMensagem())
                .status(StatusSolicitacao.PENDENTE)
                .build();

        SolicitacaoOrientacao solicitacaoSalva = solicitacaoRepository.save(solicitacao);

        return new SolicitacaoResponseDTO(
                solicitacaoSalva.getId(),
                solicitacaoSalva.getOrientador().getId(),
                solicitacaoSalva.getAluno().getId(),
                solicitacaoSalva.getTema(),
                solicitacaoSalva.getMensagem(),
                solicitacaoSalva.getJustificativa(),
                solicitacaoSalva.getStatus().name(),
                solicitacaoSalva.getCriadoEm() != null ? solicitacaoSalva.getCriadoEm().toString() : null
        );
    }
}