package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class EntregaRepositoryTest {

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Aluno aluno;
    private Orientador orientador;
    private TurmaTCC turma;

    @BeforeEach
    void setUp() {
        orientador = Orientador.builder()
                .nome("Prof. Carlos Silva")
                .email("carlos@ifsc.edu.br")
                .departamento("DAELN")
                .ativo(true)
                .build();
        entityManager.persistAndFlush(orientador);

        aluno = Aluno.builder()
                .nome("Maria Souza")
                .email("maria@ifsc.edu.br")
                .curso("Análise e Desenvolvimento de Sistemas")
                .build();
        entityManager.persistAndFlush(aluno);

        turma = TurmaTCC.builder()
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(orientador)
                .build();
        entityManager.persistAndFlush(turma);
    }

    private Entrega salvarEntrega(Aluno a, Orientador o, TurmaTCC t, TipoEntrega tipo, StatusEntrega status, String url, boolean atrasada) {
        Entrega entrega = Entrega.builder()
                .aluno(a)
                .orientador(o)
                .turma(t)
                .tipo(tipo)
                .status(status)
                .urlDocumento(url)
                .atrasada(atrasada)
                .build();
        return entityManager.persistAndFlush(entrega);
    }

    @Test
    @DisplayName("Deve persistir e recuperar uma entrega com todos os campos")
    void salvarERecuperar_DevePersistirComSucesso() {
        Entrega salva = salvarEntrega(
                aluno, orientador, turma,
                TipoEntrega.PROJETO_TCC,
                StatusEntrega.SUBMETIDO,
                "https://docs.google.com/document/d/12345/edit",
                false
        );

        Optional<Entrega> encontrada = entregaRepository.findById(salva.getId());

        assertTrue(encontrada.isPresent());
        Entrega e = encontrada.get();
        assertThat(e.getAluno().getId()).isEqualTo(aluno.getId());
        assertThat(e.getOrientador().getId()).isEqualTo(orientador.getId());
        assertThat(e.getTurma().getId()).isEqualTo(turma.getId());
        assertThat(e.getTipo()).isEqualTo(TipoEntrega.PROJETO_TCC);
        assertThat(e.getStatus()).isEqualTo(StatusEntrega.SUBMETIDO);
        assertThat(e.getUrlDocumento()).isEqualTo("https://docs.google.com/document/d/12345/edit");
        assertThat(e.getAtrasada()).isFalse();
        assertThat(e.getEnviadoEm()).isNotNull();
    }

    @Test
    @DisplayName("Deve encontrar entrega por alunoId, turmaId e tipo quando existir")
    void findByAlunoIdAndTurmaIdAndTipo_QuandoExiste_DeveRetornarEntrega() {
        salvarEntrega(aluno, orientador, turma, TipoEntrega.TCC1, StatusEntrega.SUBMETIDO, "https://github.com/org/repo", false);

        Optional<Entrega> encontrada = entregaRepository.findByAlunoIdAndTurmaIdAndTipo(aluno.getId(), turma.getId(), TipoEntrega.TCC1);

        assertTrue(encontrada.isPresent());
        assertThat(encontrada.get().getTipo()).isEqualTo(TipoEntrega.TCC1);
    }

    @Test
    @DisplayName("Deve retornar vazio quando pesquisar por combinação inexistente de aluno, turma e tipo")
    void findByAlunoIdAndTurmaIdAndTipo_QuandoNaoExiste_DeveRetornarVazio() {
        Optional<Entrega> encontrada = entregaRepository.findByAlunoIdAndTurmaIdAndTipo(aluno.getId(), turma.getId(), TipoEntrega.TCC2);

        assertFalse(encontrada.isPresent());
    }

    @Test
    @DisplayName("Deve verificar existência corretamente por alunoId, turmaId e tipo")
    void existsByAlunoIdAndTurmaIdAndTipo_DeveRetornarTrueQuandoExisteEFalseQuandoNaoExiste() {
        salvarEntrega(aluno, orientador, turma, TipoEntrega.PROJETO_TCC, StatusEntrega.SUBMETIDO, "https://docs.google.com/doc", false);

        assertTrue(entregaRepository.existsByAlunoIdAndTurmaIdAndTipo(aluno.getId(), turma.getId(), TipoEntrega.PROJETO_TCC));
        assertFalse(entregaRepository.existsByAlunoIdAndTurmaIdAndTipo(aluno.getId(), turma.getId(), TipoEntrega.TCC2));
    }

    @Test
    @DisplayName("Deve listar entregas do aluno ordenadas por tipo")
    void findByAlunoIdOrderByTipoAsc_DeveRetornarEntregasDoAlunoOrdenadas() {
        salvarEntrega(aluno, orientador, turma, TipoEntrega.TCC1, StatusEntrega.SUBMETIDO, "https://docs.google.com/tcc1", false);
        salvarEntrega(aluno, orientador, turma, TipoEntrega.PROJETO_TCC, StatusEntrega.APROVADO, "https://docs.google.com/projeto", false);

        List<Entrega> entregas = entregaRepository.findByAlunoIdOrderByTipoAsc(aluno.getId());

        assertThat(entregas).hasSize(2);
        assertThat(entregas.get(0).getTipo()).isEqualTo(TipoEntrega.PROJETO_TCC);
        assertThat(entregas.get(1).getTipo()).isEqualTo(TipoEntrega.TCC1);
    }

    @Test
    @DisplayName("Deve listar entregas sob orientação do professor ordenadas por data de envio decrescente")
    void findByOrientadorIdOrderByEnviadoEmDesc_DeveRetornarEntregasDoOrientador() {
        Aluno outroAluno = Aluno.builder()
                .nome("João Pedro")
                .email("joao@ifsc.edu.br")
                .curso("ADS")
                .build();
        entityManager.persistAndFlush(outroAluno);

        salvarEntrega(aluno, orientador, turma, TipoEntrega.PROJETO_TCC, StatusEntrega.SUBMETIDO, "https://link1.com", false);
        salvarEntrega(outroAluno, orientador, turma, TipoEntrega.PROJETO_TCC, StatusEntrega.SUBMETIDO, "https://link2.com", true);

        List<Entrega> entregas = entregaRepository.findByOrientadorIdOrderByEnviadoEmDesc(orientador.getId());

        assertThat(entregas).hasSize(2);
        assertThat(entregas).extracting(Entrega::getUrlDocumento)
                .containsExactlyInAnyOrder("https://link1.com", "https://link2.com");
    }

    @Test
    @DisplayName("Deve filtrar entregas do orientador pelo status")
    void findByOrientadorIdAndStatusOrderByEnviadoEmDesc_DeveFiltrarPorStatus() {
        salvarEntrega(aluno, orientador, turma, TipoEntrega.PROJETO_TCC, StatusEntrega.AJUSTES_NECESSARIOS, "https://link1.com", false);
        salvarEntrega(aluno, orientador, turma, TipoEntrega.TCC1, StatusEntrega.SUBMETIDO, "https://link2.com", false);

        List<Entrega> pendentes = entregaRepository.findByOrientadorIdAndStatusOrderByEnviadoEmDesc(orientador.getId(), StatusEntrega.SUBMETIDO);
        List<Entrega> ajustes = entregaRepository.findByOrientadorIdAndStatusOrderByEnviadoEmDesc(orientador.getId(), StatusEntrega.AJUSTES_NECESSARIOS);

        assertThat(pendentes).hasSize(1);
        assertThat(pendentes.get(0).getTipo()).isEqualTo(TipoEntrega.TCC1);

        assertThat(ajustes).hasSize(1);
        assertThat(ajustes.get(0).getTipo()).isEqualTo(TipoEntrega.PROJETO_TCC);
    }

    @Test
    @DisplayName("Deve lançar exceção ao violar a constraint única uk_entrega_aluno_turma_tipo")
    void violacaoConstraintUnica_AlunoTurmaTipo_DeveLancarExcecao() {
        salvarEntrega(aluno, orientador, turma, TipoEntrega.PROJETO_TCC, StatusEntrega.SUBMETIDO, "https://link1.com", false);

        Entrega duplicada = Entrega.builder()
                .aluno(aluno)
                .orientador(orientador)
                .turma(turma)
                .tipo(TipoEntrega.PROJETO_TCC)
                .status(StatusEntrega.SUBMETIDO)
                .urlDocumento("https://link2.com")
                .atrasada(true)
                .build();

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicada);
        });
    }
}

