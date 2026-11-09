package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.url=jdbc:h2:mem:parecer_flyway_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ParecerRepositoryTest {

    @Autowired
    private ParecerRepository parecerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Orientador orientador;
    private Entrega entrega;

    @BeforeEach
    void setUp() {
        orientador = Orientador.builder()
                .nome("Prof. Carlos Silva")
                .email("carlos@ifsc.edu.br")
                .departamento("DAELN")
                .ativo(true)
                .build();
        entityManager.persistAndFlush(orientador);

        Aluno aluno = Aluno.builder()
                .nome("Maria Souza")
                .email("maria@ifsc.edu.br")
                .curso("Análise e Desenvolvimento de Sistemas")
                .build();
        entityManager.persistAndFlush(aluno);

        TurmaTCC turma = TurmaTCC.builder()
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(orientador)
                .build();
        entityManager.persistAndFlush(turma);

        entrega = Entrega.builder()
                .aluno(aluno)
                .orientador(orientador)
                .turma(turma)
                .tipo(TipoEntrega.PROJETO_TCC)
                .status(StatusEntrega.SUBMETIDO)
                .urlDocumento("https://docs.google.com/document/d/12345/edit")
                .atrasada(false)
                .enviadoEm(Instant.parse("2026-11-03T14:20:00Z"))
                .build();
        entityManager.persistAndFlush(entrega);
    }

    @Test
    @DisplayName("Deve persistir e recuperar um parecer com comentário e timestamp explícito")
    void salvarERecuperar_ComComentario_DevePersistirComSucesso() {
        Instant registradoEm = Instant.parse("2026-11-03T16:00:00Z");
        Parecer parecer = Parecer.builder()
                .entrega(entrega)
                .orientador(orientador)
                .resultado(StatusEntrega.AJUSTES_NECESSARIOS)
                .comentario("Detalhar melhor a seção de metodologia.")
                .registradoEm(registradoEm)
                .build();

        Parecer salvo = entityManager.persistAndFlush(parecer);
        Optional<Parecer> encontrado = parecerRepository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        Parecer p = encontrado.get();
        assertThat(p.getEntrega().getId()).isEqualTo(entrega.getId());
        assertThat(p.getOrientador().getId()).isEqualTo(orientador.getId());
        assertThat(p.getResultado()).isEqualTo(StatusEntrega.AJUSTES_NECESSARIOS);
        assertThat(p.getComentario()).isEqualTo("Detalhar melhor a seção de metodologia.");
        assertThat(p.getRegistradoEm()).isEqualTo(registradoEm);
    }

    @Test
    @DisplayName("Deve persistir parecer aprovado sem comentário e preencher registradoEm automaticamente quando omitido")
    void salvar_SemComentarioESemRegistradoEm_DevePreencherTimestampAutomaticamente() {
        Parecer parecer = Parecer.builder()
                .entrega(entrega)
                .orientador(orientador)
                .resultado(StatusEntrega.APROVADO)
                .build();

        Parecer salvo = entityManager.persistAndFlush(parecer);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getComentario()).isNull();
        assertThat(salvo.getRegistradoEm()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar o parecer mais recente da entrega mantendo o histórico de avaliações")
    void findFirstByEntregaIdOrderByRegistradoEmDesc_DeveRetornarMaisRecente() {
        Parecer primeiro = Parecer.builder()
                .entrega(entrega)
                .orientador(orientador)
                .resultado(StatusEntrega.AJUSTES_NECESSARIOS)
                .comentario("Ajustar cronograma.")
                .registradoEm(Instant.parse("2026-11-03T16:00:00Z"))
                .build();
        entityManager.persistAndFlush(primeiro);

        Parecer segundo = Parecer.builder()
                .entrega(entrega)
                .orientador(orientador)
                .resultado(StatusEntrega.APROVADO)
                .comentario("Ajustes atendidos com sucesso.")
                .registradoEm(Instant.parse("2026-11-04T10:30:00Z"))
                .build();
        entityManager.persistAndFlush(segundo);

        Optional<Parecer> ultimo = parecerRepository.findFirstByEntregaIdOrderByRegistradoEmDesc(entrega.getId());
        List<Parecer> historico = parecerRepository.findByEntregaIdOrderByRegistradoEmDesc(entrega.getId());

        assertTrue(ultimo.isPresent());
        assertThat(ultimo.get().getResultado()).isEqualTo(StatusEntrega.APROVADO);
        assertThat(ultimo.get().getComentario()).isEqualTo("Ajustes atendidos com sucesso.");

        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).getResultado()).isEqualTo(StatusEntrega.APROVADO);
        assertThat(historico.get(1).getResultado()).isEqualTo(StatusEntrega.AJUSTES_NECESSARIOS);
    }

    @Test
    @DisplayName("Deve retornar vazio quando a entrega não possuir pareceres registrados")
    void findFirstByEntregaIdOrderByRegistradoEmDesc_QuandoSemParecer_DeveRetornarVazio() {
        Optional<Parecer> ultimo = parecerRepository.findFirstByEntregaIdOrderByRegistradoEmDesc(entrega.getId());

        assertFalse(ultimo.isPresent());
    }
}

