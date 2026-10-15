package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.AcessoNegadoException;
import br.edu.ifsc.gestao_tcc.exception.PrazoJaCadastradoException;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrazoEntregaService {

    private static final String MENSAGEM_ACESSO_NEGADO =
            "Somente o professor responsável pela Turma_TCC vigente pode gerenciar os prazos de entrega.";


    private final PrazoEntregaRepository prazoEntregaRepository;
    private final TurmaTCCRepository turmaTCCRepository;
    private final OrientadorRepository orientadorRepository;

    @Transactional
    public PrazoEntregaResponseDTO cadastrar(String emailProfessor, PrazoEntregaRequestDTO dto) {
        TurmaTCC turma = buscarTurmaVigenteDoResponsavel(emailProfessor);

        if (prazoEntregaRepository.existsByTurmaIdAndTipo(turma.getId(), dto.tipo())) {
            throw new PrazoJaCadastradoException("Já existe um prazo cadastrado para "
                    + dto.tipo().getDescricao() + " na turma " + turma.getSemestre()
                    + ". Edite o prazo existente.");
        }

        PrazoEntrega prazo = PrazoEntrega.builder()
                .turma(turma)
                .tipo(dto.tipo())
                .dataLimite(dto.dataLimite())
                .build();

        return toResponse(prazoEntregaRepository.save(prazo));
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
