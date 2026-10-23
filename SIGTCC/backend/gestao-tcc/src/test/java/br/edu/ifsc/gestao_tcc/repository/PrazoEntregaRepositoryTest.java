package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PrazoEntregaRepositoryTest {

    @Autowired
    private PrazoEntregaRepository prazoEntregaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TurmaTCC criarTurma(String semestre, boolean vigente, String emailProfessor) {
        Orientador professor = new Orientador();
        professor.setNome("Prof. " + semestre);
        professor.setEmail(emailProfessor);
        professor.setDepartamento("DAELN");
        professor.setAtivo(true);
        entityManager.persistAndFlush(professor);

        TurmaTCC turma = TurmaTCC.builder()
                .semestre(semestre)
                .vigente(vigente)
                .professorResponsavel(professor)
                .build();
        return entityManager.persistAndFlush(turma);
    }

    private PrazoEntrega salvarPrazo(TurmaTCC turma, TipoEntrega tipo, LocalDate dataLimite) {
        PrazoEntrega prazo = PrazoEntrega.builder()
                .turma(turma)
                .tipo(tipo)
                .dataLimite(dataLimite)
                .build();
        return entityManager.persistAndFlush(prazo);
    }

    @Test
    @DisplayName("Deve retornar todos os prazos vinculados à turma informada")
    void findByTurmaId_DeveRetornarApenasPrazosDaTurma() {
        TurmaTCC turmaAtual = criarTurma("2026/2", true, "prof1@ifsc.edu.br");
        TurmaTCC outraTurma = criarTurma("2026/1", false, "prof2@ifsc.edu.br");

        salvarPrazo(turmaAtual, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 10));
        salvarPrazo(turmaAtual, TipoEntrega.TCC1, LocalDate.of(2026, 11, 30));
        salvarPrazo(outraTurma, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 5, 10));

        List<PrazoEntrega> prazos = prazoEntregaRepository.findByTurmaId(turmaAtual.getId());

        assertThat(prazos).hasSize(2);
        assertThat(prazos)
                .extracting(PrazoEntrega::getTipo)
                .containsExactlyInAnyOrder(TipoEntrega.PROJETO_TCC, TipoEntrega.TCC1);
    }

    @Test
    @DisplayName("Deve verificar corretamente se já existe prazo cadastrado para a turma e tipo de entrega")
    void existsByTurmaIdAndTipo_DeveRetornarTrueQuandoExisteEFalseQuandoNaoExiste() {
        TurmaTCC turma = criarTurma("2026/2", true, "prof@ifsc.edu.br");
        salvarPrazo(turma, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 10));

        assertTrue(prazoEntregaRepository.existsByTurmaIdAndTipo(turma.getId(), TipoEntrega.PROJETO_TCC));
        assertFalse(prazoEntregaRepository.existsByTurmaIdAndTipo(turma.getId(), TipoEntrega.TCC1));
        assertFalse(prazoEntregaRepository.existsByTurmaIdAndTipo(turma.getId(), TipoEntrega.TCC2));
    }
}   