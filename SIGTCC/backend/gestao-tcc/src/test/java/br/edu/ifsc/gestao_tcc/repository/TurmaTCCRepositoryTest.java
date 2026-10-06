package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TurmaTCCRepositoryTest {

    @Autowired
    private TurmaTCCRepository turmaTCCRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Orientador salvarOrientador(String nome, String email) {
        Orientador orientador = new Orientador();
        orientador.setNome(nome);
        orientador.setEmail(email);
        orientador.setDepartamento("DAELN");
        orientador.setAtivo(true);
        return entityManager.persistAndFlush(orientador);
    }

    private TurmaTCC salvarTurma(String semestre, boolean vigente, Orientador professor) {
        TurmaTCC turma = TurmaTCC.builder()
                .semestre(semestre)
                .vigente(vigente)
                .professorResponsavel(professor)
                .build();
        return entityManager.persistAndFlush(turma);
    }

    @Test
    @DisplayName("Deve retornar true quando o professor for o responsável pela turma vigente")
    void existsByProfessorResponsavelIdAndVigenteTrue_QuandoProfessorEResponsavelPorTurmaVigente_DeveRetornarTrue() {
        Orientador professor = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");
        salvarTurma("2026/2", true, professor);

        boolean eResponsavel = turmaTCCRepository.existsByProfessorResponsavelIdAndVigenteTrue(professor.getId());

        assertTrue(eResponsavel);
    }

    @Test
    @DisplayName("Deve retornar false quando o professor for responsável apenas por uma turma não vigente")
    void existsByProfessorResponsavelIdAndVigenteTrue_QuandoProfessorEResponsavelMasTurmaNaoVigente_DeveRetornarFalse() {
        Orientador professor = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");
        salvarTurma("2026/1", false, professor);

        boolean eResponsavel = turmaTCCRepository.existsByProfessorResponsavelIdAndVigenteTrue(professor.getId());

        assertFalse(eResponsavel);
    }

    @Test
    @DisplayName("Deve retornar false quando outro professor for o responsável pela turma vigente")
    void existsByProfessorResponsavelIdAndVigenteTrue_QuandoOutroProfessorForResponsavel_DeveRetornarFalse() {
        Orientador responsavel = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");
        Orientador outroProfessor = salvarOrientador("Prof. Ana", "ana@ifsc.edu.br");
        salvarTurma("2026/2", true, responsavel);

        boolean eResponsavel = turmaTCCRepository.existsByProfessorResponsavelIdAndVigenteTrue(outroProfessor.getId());

        assertFalse(eResponsavel);
    }

    @Test
    @DisplayName("Deve retornar false quando não houver turmas cadastradas para o professor")
    void existsByProfessorResponsavelIdAndVigenteTrue_QuandoNaoExisteTurma_DeveRetornarFalse() {
        Orientador professor = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");

        boolean eResponsavel = turmaTCCRepository.existsByProfessorResponsavelIdAndVigenteTrue(professor.getId());

        assertFalse(eResponsavel);
    }

    @Test
    @DisplayName("Deve retornar a turma vigente quando ela existir")
    void findByVigenteTrue_QuandoExisteTurmaVigente_DeveRetornarTurma() {
        Orientador professor = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");
        salvarTurma("2026/2", true, professor);

        Optional<TurmaTCC> turmaVigente = turmaTCCRepository.findByVigenteTrue();

        assertTrue(turmaVigente.isPresent());
        assertThat(turmaVigente.get().getSemestre()).isEqualTo("2026/2");
        assertThat(turmaVigente.get().getProfessorResponsavel().getId()).isEqualTo(professor.getId());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando não houver nenhuma turma vigente")
    void findByVigenteTrue_QuandoNaoExisteTurmaVigente_DeveRetornarVazio() {
        Orientador professor = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");
        salvarTurma("2026/1", false, professor);

        Optional<TurmaTCC> turmaVigente = turmaTCCRepository.findByVigenteTrue();

        assertFalse(turmaVigente.isPresent());
    }

    @Test
    @DisplayName("Deve encontrar a turma pelo semestre especificado")
    void findBySemestre_QuandoSemestreExiste_DeveRetornarTurma() {
        Orientador professor = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");
        salvarTurma("2026/1", false, professor);

        Optional<TurmaTCC> turma = turmaTCCRepository.findBySemestre("2026/1");

        assertTrue(turma.isPresent());
        assertThat(turma.get().getSemestre()).isEqualTo("2026/1");
    }

    @Test
    @DisplayName("Deve retornar a lista de turmas sob a responsabilidade de um professor")
    void findByProfessorResponsavelId_DeveRetornarListaDeTurmasDoProfessor() {
        Orientador professor = salvarOrientador("Prof. Carlos", "carlos@ifsc.edu.br");
        salvarTurma("2026/1", false, professor);
        salvarTurma("2026/2", true, professor);

        List<TurmaTCC> turmas = turmaTCCRepository.findByProfessorResponsavelId(professor.getId());

        assertThat(turmas).hasSize(2);
    }
}
