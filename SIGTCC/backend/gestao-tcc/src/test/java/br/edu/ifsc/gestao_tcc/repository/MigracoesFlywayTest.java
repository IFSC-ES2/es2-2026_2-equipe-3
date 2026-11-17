package br.edu.ifsc.gestao_tcc.repository;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executa todas as migrations do Flyway em um banco vazio, como acontece no staging e no
 * {@code docker compose up}, e confere o estado deixado pelo seed V5 (BUG-15, issue #274).
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.url=jdbc:h2:mem:migracoes_flyway_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MigracoesFlywayTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Todas as migrations devem ser aplicadas com sucesso em um banco vazio")
    void todasAsMigrations_DevemSerAplicadasSemErro() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        assertThat(flyway.info().pending()).isEmpty();
        assertThat(aplicadas).isNotEmpty();
        assertThat(aplicadas).allMatch(info -> info.getState().isApplied() && !info.getState().isFailed());
        assertThat(Arrays.stream(aplicadas).map(info -> info.getVersion().getVersion()))
                .contains("1", "5", "9");
    }

    @Test
    @DisplayName("Em um banco novo, o seed V5 deve criar apenas os dois professores do seed")
    void seedV5_EmBancoNovo_DeveCriarProfessoresDoSeed() {
        List<String> emails = jdbcTemplate.queryForList("SELECT email FROM usuarios ORDER BY id", String.class);
        assertThat(emails).containsExactly(
                "professor.orientador@example.com",
                "professor.responsavel@example.com");

        Integer orientadoresAtivos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orientadores WHERE ativo = true", Integer.class);
        assertThat(orientadoresAtivos).isEqualTo(2);

        List<Integer> vagas = jdbcTemplate.queryForList(
                "SELECT vagas_disponiveis FROM perfis_professores ORDER BY orientador_id", Integer.class);
        assertThat(vagas).containsExactly(3, 3);

        Map<String, Object> turma = jdbcTemplate.queryForMap(
                "SELECT semestre, vigente, professor_responsavel_id FROM turmas_tcc");
        assertThat(turma.get("SEMESTRE")).isEqualTo("2026/2");
        assertThat(turma.get("VIGENTE")).isEqualTo(true);
        assertThat(((Number) turma.get("PROFESSOR_RESPONSAVEL_ID")).longValue()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Em um banco novo, o seed V5 não deve transformar nenhum usuário em aluno")
    void seedV5_EmBancoNovo_NaoDeveCriarAlunos() {
        Integer alunos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM alunos", Integer.class);

        assertThat(alunos).isZero();
    }
}
