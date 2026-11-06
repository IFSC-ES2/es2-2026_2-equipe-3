package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.EntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.EntregaResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.SubmissaoEntregaResultado;
import br.edu.ifsc.gestao_tcc.exception.AcessoNegadoException;
import br.edu.ifsc.gestao_tcc.exception.ConflitoDeEstadoException;
import br.edu.ifsc.gestao_tcc.exception.RegraDeNegocioException;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Entrega;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusEntrega;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.EntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntregaService {

    private static final String MENSAGEM_ACESSO_NEGADO =
            "Somente alunos com orientador vinculado (solicitação aceita) podem submeter entregas.";
    private static final String MENSAGEM_CONSULTA_NEGADA =
            "Acesso permitido apenas ao aluno autor ou ao orientador vinculado a esta entrega.";

    private final EntregaRepository entregaRepository;
    private final AlunoRepository alunoRepository;
    private final OrientadorRepository orientadorRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final TurmaTCCRepository turmaTCCRepository;
    private final PrazoEntregaRepository prazoEntregaRepository;
    private final Clock clock;

    @Transactional
    public SubmissaoEntregaResultado submeter(String emailAluno, EntregaRequestDTO dto) {
        SolicitacaoOrientacao vinculo = buscarVinculoAtivo(emailAluno);
        Aluno aluno = vinculo.getAluno();

        TurmaTCC turma = turmaTCCRepository.findByVigenteTrue()
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Não há turma de TCC vigente cadastrada para receber entregas."));

        PrazoEntrega prazo = prazoEntregaRepository.findByTurmaIdAndTipo(turma.getId(), dto.tipo())
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Não há prazo cadastrado para " + dto.tipo().getDescricao() + " na turma vigente."));

        boolean atrasada = LocalDate.now(clock).isAfter(prazo.getDataLimite());
        Instant enviadoEm = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);

        Optional<Entrega> existente =
                entregaRepository.findByAlunoIdAndTurmaIdAndTipo(aluno.getId(), turma.getId(), dto.tipo());

        if (existente.isPresent()) {
            return new SubmissaoEntregaResultado(
                    toResponse(reenviar(existente.get(), dto.urlDocumento(), atrasada, enviadoEm)), false);
        }

        Entrega nova = Entrega.builder()
                .aluno(aluno)
                .orientador(vinculo.getOrientador())
                .turma(turma)
                .tipo(dto.tipo())
                .status(StatusEntrega.SUBMETIDO)
                .urlDocumento(dto.urlDocumento())
                .atrasada(atrasada)
                .enviadoEm(enviadoEm)
                .build();

        return new SubmissaoEntregaResultado(toResponse(salvarNova(nova)), true);
    }

    @Transactional(readOnly = true)
    public EntregaResponseDTO buscarPorId(String email, Long id) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entrega com identificador " + id + " não foi encontrada."));

        boolean autor = entrega.getAluno().getEmail().equalsIgnoreCase(email);
        boolean orientadorVinculado = entrega.getOrientador().getEmail().equalsIgnoreCase(email);

        if (!autor && !orientadorVinculado) {
            throw new AcessoNegadoException(MENSAGEM_CONSULTA_NEGADA);
        }

        return toResponse(entrega);
    }

    @Transactional(readOnly = true)
    public List<EntregaResponseDTO> listarDoOrientador(String email, StatusEntrega status) {
        if (email == null || email.isBlank()) {
            throw new ResourceNotFoundException("Não foi possível identificar o professor autenticado.");
        }

        Orientador orientador = orientadorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orientador com e-mail " + email + " não foi encontrado."));

        List<Entrega> entregas = status == null
                ? entregaRepository.findByOrientadorIdOrderByEnviadoEmDesc(orientador.getId())
                : entregaRepository.findByOrientadorIdAndStatusOrderByEnviadoEmDesc(orientador.getId(), status);

        return entregas.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EntregaResponseDTO> listarDoAluno(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        return alunoRepository.findByEmail(email)
                .map(aluno -> entregaRepository.findByAlunoIdOrderByTipoAsc(aluno.getId()).stream()
                        .map(this::toResponse)
                        .toList())
                .orElse(List.of());
    }

    private SolicitacaoOrientacao buscarVinculoAtivo(String emailAluno) {
        if (emailAluno == null || emailAluno.isBlank()) {
            throw new AcessoNegadoException(MENSAGEM_ACESSO_NEGADO);
        }

        return alunoRepository.findByEmail(emailAluno)
                .flatMap(aluno -> solicitacaoRepository.findFirstByAlunoIdAndStatusOrderByCriadoEmDesc(
                        aluno.getId(), StatusSolicitacao.ACEITA))
                .orElseThrow(() -> new AcessoNegadoException(MENSAGEM_ACESSO_NEGADO));
    }

    private Entrega reenviar(Entrega entrega, String urlDocumento, boolean atrasada, Instant enviadoEm) {
        if (entrega.getStatus() != StatusEntrega.AJUSTES_NECESSARIOS) {
            throw conflito(entrega.getTipo(), entrega.getStatus());
        }

        entrega.setUrlDocumento(urlDocumento);
        entrega.setStatus(StatusEntrega.SUBMETIDO);
        entrega.setAtrasada(atrasada);
        entrega.setEnviadoEm(enviadoEm);
        return entregaRepository.save(entrega);
    }

    private Entrega salvarNova(Entrega entrega) {
        try {
            return entregaRepository.saveAndFlush(entrega);
        } catch (DataIntegrityViolationException ex) {
            throw conflito(entrega.getTipo(), StatusEntrega.SUBMETIDO);
        }
    }

    private ConflitoDeEstadoException conflito(TipoEntrega tipo, StatusEntrega status) {
        return new ConflitoDeEstadoException("Já existe uma entrega para " + tipo.getDescricao()
                + " com status " + status + ". O reenvio só é permitido quando o status for AJUSTES_NECESSARIOS.");
    }

    private EntregaResponseDTO toResponse(Entrega entrega) {
        Aluno aluno = entrega.getAluno();
        Orientador orientador = entrega.getOrientador();

        return new EntregaResponseDTO(
                entrega.getId(),
                entrega.getTurma().getSemestre(),
                entrega.getTipo(),
                entrega.getTipo().getDescricao(),
                entrega.getUrlDocumento(),
                entrega.getStatus(),
                entrega.getAtrasada(),
                entrega.getEnviadoEm(),
                new EntregaResponseDTO.AlunoResponse(
                        aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getCurso()),
                new EntregaResponseDTO.OrientadorResponse(
                        orientador.getId(), orientador.getNome(), orientador.getEmail()),
                null
        );
    }
}
