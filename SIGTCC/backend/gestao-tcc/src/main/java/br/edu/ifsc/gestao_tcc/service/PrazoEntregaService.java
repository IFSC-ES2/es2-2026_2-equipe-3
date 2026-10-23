package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaUpdateDTO;
import br.edu.ifsc.gestao_tcc.exception.*;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrazoEntregaService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String MENSAGEM_ACESSO_NEGADO =
            "Somente o professor responsável pela Turma_TCC vigente pode gerenciar os prazos de entrega.";


    private final PrazoEntregaRepository prazoEntregaRepository;
    private final TurmaTCCRepository turmaTCCRepository;
    private final OrientadorRepository orientadorRepository;
    private final Clock clock;


    @Transactional
    public PrazoEntregaResponseDTO cadastrar(String emailProfessor, PrazoEntregaRequestDTO dto) {
        TurmaTCC turma = buscarTurmaVigenteDoResponsavel(emailProfessor);

        if (prazoEntregaRepository.existsByTurmaIdAndTipo(turma.getId(), dto.tipo())) {
            throw new PrazoJaCadastradoException("Já existe um prazo cadastrado para "
                    + dto.tipo().getDescricao() + " na turma " + turma.getSemestre()
                    + ". Edite o prazo existente.");
        }

        validarDataLimite(dto.tipo(), dto.dataLimite(), prazoEntregaRepository.findByTurmaId(turma.getId()), null);

        PrazoEntrega prazo = PrazoEntrega.builder()
                .turma(turma)
                .tipo(dto.tipo())
                .dataLimite(dto.dataLimite())
                .build();

        return toResponse(prazoEntregaRepository.save(prazo));
    }

    @Transactional
    public PrazoEntregaResponseDTO atualizar(String emailProfessor, Long prazoId, PrazoEntregaUpdateDTO dto) {
        TurmaTCC turma = buscarTurmaVigenteDoResponsavel(emailProfessor);

        PrazoEntrega prazo = prazoEntregaRepository.findById(prazoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prazo de entrega com identificador " + prazoId + " não foi encontrado."));

        if (!Objects.equals(prazo.getTurma().getId(), turma.getId())) {
            throw new RegraDeNegocioException("Somente prazos da Turma_TCC vigente podem ser alterados.");
        }

        validarDataLimite(prazo.getTipo(), dto.dataLimite(),
                prazoEntregaRepository.findByTurmaId(turma.getId()), prazo.getId());

        prazo.setDataLimite(dto.dataLimite());
        return toResponse(prazoEntregaRepository.save(prazo));
    }

    @Transactional(readOnly = true)
    public List<PrazoEntregaResponseDTO> listarPrazosVigentes() {
        return turmaTCCRepository.findByVigenteTrue()
                .map(turma -> prazoEntregaRepository.findByTurmaId(turma.getId()).stream()
                        .sorted(Comparator.comparing(PrazoEntrega::getTipo))
                        .map(this::toResponse)
                        .toList())
                .orElse(List.of());
    }

    private TurmaTCC buscarTurmaVigenteDoResponsavel(String emailProfessor) {
        Optional<Orientador> professor = (emailProfessor == null || emailProfessor.isBlank())
                ? Optional.empty()
                : orientadorRepository.findByEmailIgnoreCase(emailProfessor);

        if (professor.isEmpty()) {
            throw new AcessoNegadoException(MENSAGEM_ACESSO_NEGADO);
        }

        return turmaTCCRepository.findByVigenteTrue()
                .filter(turma -> Objects.equals(turma.getProfessorResponsavel().getId(), professor.get().getId()))
                .orElseThrow(() -> new AcessoNegadoException(MENSAGEM_ACESSO_NEGADO));
    }

    private void validarDataLimite(TipoEntrega tipo, LocalDate dataLimite,
                                   List<PrazoEntrega> prazosDaTurma, Long prazoEmEdicaoId) {
        if (dataLimite.isBefore(LocalDate.now(clock))) {
            throw new ValidacaoRequisicaoException("dataLimite", "A data limite não pode estar no passado.");
        }

        for (PrazoEntrega outro : prazosDaTurma) {
            if (Objects.equals(outro.getId(), prazoEmEdicaoId) || outro.getTipo() == tipo) {
                continue;
            }

            if (outro.getTipo().vemAntesDe(tipo) && !dataLimite.isAfter(outro.getDataLimite())) {
                throw new RegraDeNegocioException(mensagemSequencia(tipo, dataLimite, "posterior", outro));
            }

            if (tipo.vemAntesDe(outro.getTipo()) && !dataLimite.isBefore(outro.getDataLimite())) {
                throw new RegraDeNegocioException(mensagemSequencia(tipo, dataLimite, "anterior", outro));
            }
        }
    }

    private String mensagemSequencia(TipoEntrega tipo, LocalDate dataLimite, String relacao, PrazoEntrega outro) {
        return "A data limite de " + tipo.getDescricao() + " (" + dataLimite.format(FORMATO_DATA) + ") deve ser "
                + relacao + " à data limite de " + outro.getTipo().getDescricao()
                + " (" + outro.getDataLimite().format(FORMATO_DATA) + ").";
    }

    private PrazoEntregaResponseDTO toResponse(PrazoEntrega prazo) {
        return new PrazoEntregaResponseDTO(
                prazo.getId(),
                prazo.getTurma().getSemestre(),
                prazo.getTipo(),
                prazo.getTipo().getDescricao(),
                prazo.getDataLimite()
        );
    }
}
