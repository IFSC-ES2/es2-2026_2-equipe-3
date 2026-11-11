package br.edu.ifsc.gestao_tcc.controller;

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
import br.edu.ifsc.gestao_tcc.repository.ParecerRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EntregaParecerIntegrationTest {

    private static final String URL = "/api/v1/entregas";
    private static final String EMAIL_ALUNO = "aluno.parecer@example.com";
    private static final String EMAIL_ORIENTADOR = "orientador.parecer@example.com";
    private static final String EMAIL_OUTRO_ORIENTADOR = "outro.orientador.parecer@example.com";

    private static final String URL_PROJETO = "https://docs.example.com/projeto-tcc";
    private static final String URL_TCC1 = "https://docs.example.com/tcc1";
    private static final String NOVA_URL_PROJETO = "https://docs.example.com/projeto-tcc-atualizado";

    private static final String COMENTARIO_AJUSTES =
            "Detalhar melhor a metodologia e os objetivos.";

    private static final String COMENTARIO_REPROVACAO =
            "O trabalho não atende aos requisitos mínimos.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private TurmaTCCRepository turmaTCCRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private PrazoEntregaRepository prazoEntregaRepository;

    @Autowired
    private ParecerRepository parecerRepository;

    private Aluno aluno;
    private Orientador orientador;
    private TurmaTCC turma;

    @BeforeEach
    void setUp() {
        orientador = new Orientador();
        orientador.setNome("Orientador de Teste");
        orientador.setEmail(EMAIL_ORIENTADOR);
        orientador.setDepartamento("DAELN");
        orientador.setAtivo(true);
        orientadorRepository.saveAndFlush(orientador);

        aluno = alunoRepository.saveAndFlush(Aluno.builder()
                .nome("Aluno de Teste")
                .email(EMAIL_ALUNO)
                .curso("Análise e Desenvolvimento de Sistemas")
                .build());

        turma = turmaTCCRepository.saveAndFlush(TurmaTCC.builder()
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(orientador)
                .build());
    }

    @Test
    @DisplayName("GET /entregas/{id} - Orientador vinculado deve consultar a entrega e obter a URL")
    void buscarPorId_OrientadorVinculado_DeveRetornarEntregaEUrl() throws Exception {
        Entrega entrega = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);

        mockMvc.perform(get(URL + "/" + entrega.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entrega.getId()))
                .andExpect(jsonPath("$.urlDocumento").value(URL_PROJETO))
                .andExpect(jsonPath("$.orientador.email").value(EMAIL_ORIENTADOR));
    }

    @Test
    @DisplayName("GET /entregas/{id} - Orientador não vinculado deve receber 403")
    void buscarPorId_OrientadorNaoVinculado_DeveRetornar403() throws Exception {
        Entrega entrega = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);
        criarOutroOrientador();

        mockMvc.perform(get(URL + "/" + entrega.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_OUTRO_ORIENTADOR)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Orientador não vinculado deve receber 403")
    void registrarParecer_OrientadorNaoVinculado_DeveRetornar403() throws Exception {
        Entrega entrega = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);
        criarOutroOrientador();

        mockMvc.perform(post(URL + "/" + entrega.getId() + "/parecer")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_OUTRO_ORIENTADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoParecer("APROVADO", null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Ajustes necessários devem liberar reenvio ao aluno")
    void registrarParecer_AjustesNecessarios_DevePermitirReenvioDoAluno() throws Exception {
        Entrega entrega = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);

        criarVinculoAceito();
        criarPrazoProjeto();

        registrarParecer(entrega.getId(), "AJUSTES_NECESSARIOS", COMENTARIO_AJUSTES)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AJUSTES_NECESSARIOS"))
                .andExpect(jsonPath("$.parecer.resultado").value("AJUSTES_NECESSARIOS"))
                .andExpect(jsonPath("$.parecer.comentario").value(COMENTARIO_AJUSTES));

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoSubmissao(NOVA_URL_PROJETO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMETIDO"))
                .andExpect(jsonPath("$.urlDocumento").value(NOVA_URL_PROJETO))
                .andExpect(jsonPath("$.parecer.resultado").value("AJUSTES_NECESSARIOS"));

        org.assertj.core.api.Assertions.assertThat(parecerRepository.count())
                .isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusEntrega.class,
            names = {"AJUSTES_NECESSARIOS", "REPROVADO"}
    )
    @DisplayName("POST /entregas/{id}/parecer - Deve exigir comentário para ajustes e reprovação")
    void registrarParecer_SemComentarioObrigatorio_DeveRetornar422(
            StatusEntrega resultado) throws Exception {

        Entrega entrega = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);

        registrarParecer(entrega.getId(), resultado.name(), null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erro").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.detalhes").value(
                        "O comentário com a justificativa é obrigatório quando o parecer exige ajustes ou reprova a entrega."));
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusEntrega.class,
            names = {"APROVADO", "REPROVADO"}
    )
    @DisplayName("POST /entregas/{id}/parecer - Não deve permitir reavaliar entrega com parecer definitivo")
    void registrarParecer_EntregaComStatusDefinitivo_DeveRetornar409(
            StatusEntrega statusDefinitivo) throws Exception {

        Entrega entrega = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);

        entrega.setStatus(statusDefinitivo);
        entregaRepository.saveAndFlush(entrega);

        registrarParecer(entrega.getId(), "APROVADO", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Conflito de dados"));
    }

    @Test
    @DisplayName("GET /entregas/minhas - Deve refletir aprovação e reprovação após os pareceres")
    void painelDoAluno_AposPareceres_DeveExibirStatusAtualizados() throws Exception {
        Entrega projeto = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);
        Entrega tcc1 = criarEntrega(TipoEntrega.TCC1, URL_TCC1);

        registrarParecer(projeto.getId(), "APROVADO", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        registrarParecer(tcc1.getId(), "REPROVADO", COMENTARIO_REPROVACAO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADO"));

        mockMvc.perform(get(URL + "/minhas")
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo",
                        hasItems("PROJETO_TCC", "TCC1")))
                .andExpect(jsonPath("$[*].status",
                        hasItems("APROVADO", "REPROVADO")))
                .andExpect(jsonPath("$[*].urlDocumento",
                        hasItems(URL_PROJETO, URL_TCC1)))
                .andExpect(jsonPath(
                        "$[?(@.tipo == 'PROJETO_TCC')].parecer.resultado",
                        hasItems("APROVADO")))
                .andExpect(jsonPath(
                        "$[?(@.tipo == 'TCC1')].parecer.resultado",
                        hasItems("REPROVADO")));
    }

    private Entrega criarEntrega(TipoEntrega tipo, String urlDocumento) {
        return entregaRepository.saveAndFlush(Entrega.builder()
                .aluno(aluno)
                .orientador(orientador)
                .turma(turma)
                .tipo(tipo)
                .status(StatusEntrega.SUBMETIDO)
                .urlDocumento(urlDocumento)
                .atrasada(false)
                .enviadoEm(Instant.parse("2026-11-09T12:00:00Z"))
                .build());
    }

    private Orientador criarOutroOrientador() {
        Orientador outro = new Orientador();
        outro.setNome("Outro Orientador");
        outro.setEmail(EMAIL_OUTRO_ORIENTADOR);
        outro.setDepartamento("DAELN");
        outro.setAtivo(true);

        return orientadorRepository.saveAndFlush(outro);
    }

    private void criarVinculoAceito() {
        solicitacaoRepository.saveAndFlush(SolicitacaoOrientacao.builder()
                .aluno(aluno)
                .orientador(orientador)
                .tema("Tema do TCC")
                .mensagem("Solicitação de orientação")
                .status(StatusSolicitacao.ACEITA)
                .build());
    }

    private void criarPrazoProjeto() {
        prazoEntregaRepository.saveAndFlush(PrazoEntrega.builder()
                .turma(turma)
                .tipo(TipoEntrega.PROJETO_TCC)
                .dataLimite(LocalDate.now().plusYears(1))
                .build());
    }

    private ResultActions registrarParecer(
            Long entregaId, String resultado, String comentario) throws Exception {

        return mockMvc.perform(post(URL + "/" + entregaId + "/parecer")
                .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoParecer(resultado, comentario)));
    }

    private String corpoParecer(String resultado, String comentario) {
        return comentario == null
                ? "{\"resultado\":\"%s\"}".formatted(resultado)
                : "{\"resultado\":\"%s\",\"comentario\":\"%s\"}"
                .formatted(resultado, comentario);
    }

    private String corpoSubmissao(String urlDocumento) {
        return "{\"tipo\":\"PROJETO_TCC\",\"urlDocumento\":\"%s\"}"
                .formatted(urlDocumento);
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email))
                .authorities(new SimpleGrantedAuthority(role));
    }
}
