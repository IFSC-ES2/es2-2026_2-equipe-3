package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SolicitacaoRepositoryTest {

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Aluno aluno;

    @BeforeEach
    void setUp() {
        aluno = entityManager.persistAndFlush(Aluno.builder()
                .nome("Maria Souza")
                .email("maria@ifsc.edu.br")
                .curso("ADS")
                .build());
    }

    private Orientador salvarOrientador(String email) {
        Orientador orientador = new Orientador();
        orientador.setNome("Prof. " + email);
        orientador.setEmail(email);
        orientador.setDepartamento("DAELN");
        orientador.setAtivo(true);
        return entityManager.persistAndFlush(orientador);
    }

    private void salvarSolicitacao(Orientador orientador, StatusSolicitacao status, Instant criadoEm) {
        SolicitacaoOrientacao solicitacao = entityManager.persistAndFlush(SolicitacaoOrientacao.builder()
                .aluno(aluno)
                .orientador(orientador)
                .tema("Tema")
                .mensagem("Mensagem")
                .status(status)
                .build());

        // criado_em é gerado pelo Hibernate e não é atualizável pela entidade.
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE solicitacoes_orientacao SET criado_em = :criadoEm WHERE id = :id")
                .setParameter("criadoEm", Timestamp.from(criadoEm))
                .setParameter("id", solicitacao.getId())
                .executeUpdate();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve retornar a solicitação aceita mais recente quando o aluno tiver mais de uma")
    void findFirst_ComDuasAceitas_DeveRetornarAMaisRecente() {
        Orientador antigo = salvarOrientador("antigo@ifsc.edu.br");
        Orientador recente = salvarOrientador("recente@ifsc.edu.br");
        salvarSolicitacao(recente, StatusSolicitacao.ACEITA, Instant.parse("2026-09-01T10:00:00Z"));
        salvarSolicitacao(antigo, StatusSolicitacao.ACEITA, Instant.parse("2026-08-01T10:00:00Z"));

        Optional<SolicitacaoOrientacao> encontrada = solicitacaoRepository
                .findFirstByAlunoIdAndStatusOrderByCriadoEmDesc(aluno.getId(), StatusSolicitacao.ACEITA);

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getOrientador().getId()).isEqualTo(recente.getId());
    }

    @Test
    @DisplayName("Deve ignorar solicitações em outros status")
    void findFirst_SemAceita_DeveRetornarVazio() {
        salvarSolicitacao(salvarOrientador("prof@ifsc.edu.br"), StatusSolicitacao.PENDENTE,
                Instant.parse("2026-09-01T10:00:00Z"));

        assertThat(solicitacaoRepository
                .findFirstByAlunoIdAndStatusOrderByCriadoEmDesc(aluno.getId(), StatusSolicitacao.ACEITA))
                .isEmpty();
    }
}
