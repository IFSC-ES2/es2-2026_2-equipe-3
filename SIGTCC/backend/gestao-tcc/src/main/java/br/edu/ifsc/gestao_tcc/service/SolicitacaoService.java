package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.AtualizaStatusRequest;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoResponseDTO;
import br.edu.ifsc.gestao_tcc.event.SolicitacaoStatusChangedEvent;
import br.edu.ifsc.gestao_tcc.exception.RegraDeNegocioException;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.exception.SolicitacaoJaRespondidaException;
import br.edu.ifsc.gestao_tcc.exception.VagasIndisponiveisException;
import br.edu.ifsc.gestao_tcc.exception.ValidacaoRequisicaoException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import br.edu.ifsc.gestao_tcc.validation.ValidacaoSolicitacaoOrientacao;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final OrientadorRepository orientadorRepository;
    private final AlunoRepository alunoRepository;
    private final ApplicationEventPublisher eventPublisher;
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

        return toResponse(solicitacaoSalva);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponseDTO> listarPorOrientador(Long orientadorId, String status) {
        Orientador orientador = orientadorRepository.findById(orientadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Orientador com identificador " + orientadorId + " não foi encontrado."));

        List<SolicitacaoOrientacao> solicitacoes;
        if (status == null || status.isBlank()) {
            solicitacoes = solicitacaoRepository.findByOrientadorIdOrderByCriadoEmDesc(orientador.getId());
        } else {
            StatusSolicitacao statusSolicitacao = converterStatus(status);
            solicitacoes = solicitacaoRepository.findByOrientadorIdAndStatusOrderByCriadoEmDesc(orientador.getId(), statusSolicitacao);
        }

        return solicitacoes.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SolicitacaoResponseDTO atualizarStatus(Long solicitacaoId, AtualizaStatusRequest request) {
        SolicitacaoOrientacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação com identificador " + solicitacaoId + " não foi encontrada."));

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new SolicitacaoJaRespondidaException("Esta solicitação foi respondida e não pode ser alterada.");
        }

        if (request == null || request.status() == null) {
            throw new ValidacaoRequisicaoException("status", "O status é obrigatório.");
        }

        StatusSolicitacao novoStatus = request.status();
        if (novoStatus != StatusSolicitacao.ACEITA && novoStatus != StatusSolicitacao.RECUSADA) {
            throw new RegraDeNegocioException("Somente os status ACEITA ou RECUSADA podem ser definidos por este endpoint.");
        }

        StatusSolicitacao statusAnterior = solicitacao.getStatus();

        if (novoStatus == StatusSolicitacao.ACEITA) {
            validarVagasDisponiveis(solicitacao.getOrientador());
            solicitacao.setStatus(StatusSolicitacao.ACEITA);
            solicitacao.setJustificativa(null);
        } else {
            validarJustificativa(request.justificativa());
            solicitacao.setStatus(StatusSolicitacao.RECUSADA);
            solicitacao.setJustificativa(request.justificativa().trim());
        }

        solicitacaoRepository.save(solicitacao);

        eventPublisher.publishEvent(new SolicitacaoStatusChangedEvent(solicitacao, statusAnterior, novoStatus));

        return toResponse(solicitacao);
    }

    private void validarVagasDisponiveis(Orientador orientador) {
        PerfilOrientador perfil = orientador.getPerfil();
        if (perfil == null || perfil.getVagasDisponiveis() <= 0) {
            throw new VagasIndisponiveisException("O orientador não possui vagas disponíveis para aceitar esta solicitação.");
        }
    }

    private void validarJustificativa(String justificativa) {
        if (justificativa == null) {
            throw new ValidacaoRequisicaoException("justificativa", "A justificativa é obrigatória para recusar a solicitação.");
        }
        String justificativaTratada = justificativa.trim();
        if (justificativaTratada.length() < 10 || justificativaTratada.length() > 500) {
            throw new ValidacaoRequisicaoException("justificativa", "A justificativa deve ter entre 10 e 500 caracteres.");
        }
    }

    private StatusSolicitacao converterStatus(String status) {
        try {
            return StatusSolicitacao.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidacaoRequisicaoException("status", "Status inválido. Valores aceitos: PENDENTE, ACEITA, RECUSADA, CANCELADA.");
        }
    }

    private SolicitacaoResponseDTO toResponse(SolicitacaoOrientacao solicitacao) {
        var aluno = solicitacao.getAluno();
        var orientador = solicitacao.getOrientador();
        int vagasDisponiveis = 0;

        if (orientador.getPerfil() != null) {
            vagasDisponiveis = orientador.getPerfil().getVagasDisponiveis();
        }

        return new SolicitacaoResponseDTO(
                solicitacao.getId(),
                solicitacao.getStatus(),
                solicitacao.getTema(),
                solicitacao.getMensagem(),
                solicitacao.getJustificativa(),
                solicitacao.getCriadoEm(),
                new SolicitacaoResponseDTO.AlunoResponse(
                        aluno.getId(),
                        aluno.getNome(),
                        aluno.getEmail(),
                        aluno.getCurso()
                ),
                new SolicitacaoResponseDTO.OrientadorResponse(
                        orientador.getId(),
                        orientador.getNome(),
                        orientador.getEmail(),
                        vagasDisponiveis
                )
        );
    }
}