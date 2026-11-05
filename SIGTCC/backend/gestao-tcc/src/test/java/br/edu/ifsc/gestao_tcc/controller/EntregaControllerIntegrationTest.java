package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.config.ClockConfig;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EntregaControllerIntegrationTest {

    private static final String URL = "/api/v1/entregas";
    private static final String EMAIL_ALUNO = "aluno@example.com";
    private static final String EMAIL_ORIENTADOR = "professor.orientador@example.com";
    private static final String URL_DOCUMENTO = "https://docs.google.com/document/d/123/edit?usp=sharing";
    private static final String NOVA_URL = "https://github.com/org/tcc";
    private static final Instant ENVIO_ANTERIOR = Instant.parse("2026-01-10T10:00:00Z");
    private static final String MENSAGEM_URL_INVALIDA =
            "A URL do documento deve ser válida e utilizar o protocolo https.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private TurmaTCCRepository turmaTCCRepository;

    @Autowired
    private PrazoEntregaRepository prazoEntregaRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    private Orientador orientador;
    private Aluno aluno;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        hoje = LocalDate.now(ClockConfig.FUSO_INSTITUICAO);

        orientador = new Orientador();
        orientador.setNome("Professor Orientador");
        orientador.setEmail(EMAIL_ORIENTADOR);
        orientador.setDepartamento("DAELN");
        orientador.setAtivo(true);
        orientadorRepository.saveAndFlush(orientador);

        aluno = alunoRepository.saveAndFlush(Aluno.builder()
                .nome("Aluno de Teste")
                .email(EMAIL_ALUNO)
                .curso("Análise e Desenvolvimento de Sistemas")
                .build());
    }

    private void comSolicitacao(StatusSolicitacao status) {
        solicitacaoRepository.saveAndFlush(SolicitacaoOrientacao.builder()
                .aluno(aluno)
                .orientador(orientador)
                .tema("Tema do TCC")
                .mensagem("Mensagem da solicitação")
                .status(status)
                .build());
    }

    private TurmaTCC comTurmaVigente() {
        return turmaTCCRepository.saveAndFlush(TurmaTCC.builder()
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(orientador)
                .build());
    }

    private TurmaTCC comPrazoProjeto(LocalDate dataLimite) {
        TurmaTCC turma = comTurmaVigente();
        prazoEntregaRepository.saveAndFlush(PrazoEntrega.builder()
                .turma(turma)
                .tipo(TipoEntrega.PROJETO_TCC)
                .dataLimite(dataLimite)
                .build());
        return turma;
    }

    private Entrega comEntrega(TurmaTCC turma, StatusEntrega status) {
        return entregaRepository.saveAndFlush(Entrega.builder()
                .aluno(aluno)
                .orientador(orientador)
                .turma(turma)
                .tipo(TipoEntrega.PROJETO_TCC)
                .status(status)
                .urlDocumento(URL_DOCUMENTO)
                .atrasada(false)
                .enviadoEm(ENVIO_ANTERIOR)
                .build());
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email))
                .authorities(new SimpleGrantedAuthority(role));
    }

    private ResultActions enviar(String payload) throws Exception {
        return mockMvc.perform(post(URL)
                .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    private String corpo(String url) {
        return """
                {
                  "tipo": "PROJETO_TCC",
                  "urlDocumento": "%s"
                }
                """.formatted(url);
    }

    @Test
    @DisplayName("POST /entregas - Deve criar a entrega no prazo (201 Created)")
    void submeter_NoPrazo_DeveRetornar201() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comPrazoProjeto(hoje.plusDays(5));

        enviar(corpo(URL_DOCUMENTO))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(URL + "/"
                        + entregaRepository.findByAlunoIdOrderByTipoAsc(aluno.getId()).getFirst().getId())))
                .andExpect(jsonPath("$.semestre").value("2026/2"))
                .andExpect(jsonPath("$.tipo").value("PROJETO_TCC"))
                .andExpect(jsonPath("$.descricaoTipo").value("Projeto de TCC"))
                .andExpect(jsonPath("$.urlDocumento").value(URL_DOCUMENTO))
                .andExpect(jsonPath("$.status").value("SUBMETIDO"))
                .andExpect(jsonPath("$.atrasada").value(false))
                .andExpect(jsonPath("$.enviadoEm").isNotEmpty())
                .andExpect(jsonPath("$.aluno.email").value(EMAIL_ALUNO))
                .andExpect(jsonPath("$.aluno.curso").value("Análise e Desenvolvimento de Sistemas"))
                .andExpect(jsonPath("$.orientador.email").value(EMAIL_ORIENTADOR))
                .andExpect(jsonPath("$.parecer").value(nullValue()));
    }

    @Test
    @DisplayName("POST /entregas - Deve aceitar o envio após o prazo marcando atraso (201 Created)")
    void submeter_AposOPrazo_DeveRetornar201ComAtraso() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comPrazoProjeto(hoje.minusDays(1));

        enviar(corpo(URL_DOCUMENTO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMETIDO"))
                .andExpect(jsonPath("$.atrasada").value(true));
    }

    @Test
    @DisplayName("POST /entregas - Deve persistir o reenvio após AJUSTES_NECESSARIOS (200 OK)")
    void submeter_ReenvioComAjustes_DeveRetornar200EAtualizarBanco() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        TurmaTCC turma = comPrazoProjeto(hoje.minusDays(1));
        Entrega existente = comEntrega(turma, StatusEntrega.AJUSTES_NECESSARIOS);

        enviar(corpo(NOVA_URL))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").value(existente.getId()))
                .andExpect(jsonPath("$.urlDocumento").value(NOVA_URL))
                .andExpect(jsonPath("$.status").value("SUBMETIDO"))
                .andExpect(jsonPath("$.atrasada").value(true));

        // Descarta o cache da sessão para ler o que realmente foi gravado no banco.
        entityManager.flush();
        entityManager.clear();

        Entrega gravada = entregaRepository.findById(existente.getId()).orElseThrow();
        assertThat(gravada.getUrlDocumento()).isEqualTo(NOVA_URL);
        assertThat(gravada.getStatus()).isEqualTo(StatusEntrega.SUBMETIDO);
        assertThat(gravada.getAtrasada()).isTrue();
        assertThat(gravada.getEnviadoEm()).isAfter(ENVIO_ANTERIOR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUBMETIDO", "APROVADO", "REPROVADO"})
    @DisplayName("POST /entregas - Deve bloquear a submissão quando a etapa já tem entrega (409 Conflict)")
    void submeter_EntregaExistente_DeveRetornar409(String statusAtual) throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        TurmaTCC turma = comPrazoProjeto(hoje.plusDays(5));
        comEntrega(turma, StatusEntrega.valueOf(statusAtual));

        enviar(corpo(NOVA_URL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Conflito de dados"))
                .andExpect(jsonPath("$.detalhes").value("Já existe uma entrega para Projeto de TCC com status "
                        + statusAtual + ". O reenvio só é permitido quando o status for AJUSTES_NECESSARIOS."));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://docs.google.com/document/1",
            "ftp://docs.google.com/document/1",
            "docs.google.com/document/1",
            "https://",
            "https://exa mple.com/doc",
            "https:///caminho",
            "https://?x=1",
            "https://#ancora",
            "https:exemplo.com",
            "https:/exemplo.com",
            "isto-nao-e-uma-url"
    })
    @DisplayName("POST /entregas - Deve rejeitar URL sem https ou malformada (400 Bad Request)")
    void submeter_UrlInvalida_DeveRetornar400(String url) throws Exception {
        enviar(corpo(url))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação nos dados enviados"))
                .andExpect(jsonPath("$.detalhes[*].campo", hasItem("urlDocumento")))
                .andExpect(jsonPath("$.detalhes[*].mensagem", hasItem(MENSAGEM_URL_INVALIDA)));
    }

    @Test
    @DisplayName("POST /entregas - Deve rejeitar URL vazia (400 Bad Request)")
    void submeter_UrlVazia_DeveRetornar400() throws Exception {
        enviar(corpo(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.length()").value(1))
                .andExpect(jsonPath("$.detalhes[0].mensagem").value("A URL do documento é obrigatória."));
    }

    @Test
    @DisplayName("POST /entregas - Deve aceitar protocolo https em letras maiúsculas (201 Created)")
    void submeter_ProtocoloEmMaiusculas_DeveRetornar201() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comPrazoProjeto(hoje.plusDays(5));

        enviar(corpo("HTTPS://exemplo.com/doc"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /entregas - Deve rejeitar URL com mais de 2048 caracteres (400 Bad Request)")
    void submeter_UrlMuitoLonga_DeveRetornar400() throws Exception {
        String prefixo = "https://exemplo.com/";

        enviar(corpo(prefixo + "a".repeat(2049 - prefixo.length())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[*].mensagem",
                        hasItem("A URL do documento deve ter no máximo 2048 caracteres.")));
    }

    @Test
    @DisplayName("POST /entregas - Deve aceitar URL com exatamente 2048 caracteres (201 Created)")
    void submeter_UrlNoLimite_DeveRetornar201() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comPrazoProjeto(hoje.plusDays(5));
        String prefixo = "https://exemplo.com/";

        enviar(corpo(prefixo + "a".repeat(2048 - prefixo.length())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /entregas - Deve rejeitar tipo ausente (400 Bad Request)")
    void submeter_TipoAusente_DeveRetornar400() throws Exception {
        enviar("""
                { "urlDocumento": "%s" }
                """.formatted(URL_DOCUMENTO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0].campo").value("tipo"))
                .andExpect(jsonPath("$.detalhes[0].mensagem").value("O tipo de entrega é obrigatório."));
    }

    @Test
    @DisplayName("POST /entregas - Deve rejeitar tipo fora da enumeração (400 Bad Request)")
    void submeter_TipoInvalido_DeveRetornar400() throws Exception {
        enviar("""
                { "tipo": "TCC3", "urlDocumento": "%s" }
                """.formatted(URL_DOCUMENTO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido ou ilegível"));
    }

    @Test
    @DisplayName("POST /entregas - Deve negar aluno sem solicitação aceita (403 Forbidden)")
    void submeter_SemSolicitacaoAceita_DeveRetornar403() throws Exception {
        comSolicitacao(StatusSolicitacao.PENDENTE);
        comPrazoProjeto(hoje.plusDays(5));

        enviar(corpo(URL_DOCUMENTO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Somente alunos com orientador vinculado (solicitação aceita) podem submeter entregas."));
    }

    @Test
    @DisplayName("POST /entregas - Deve negar aluno sem cadastro (403 Forbidden)")
    void submeter_AlunoSemCadastro_DeveRetornar403() throws Exception {
        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", "sem.cadastro@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(URL_DOCUMENTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    /**
     * Verifica apenas o status: a resposta sem corpo prevista no contrato depende da
     * regra de ROLE_ALUNO em SecurityConfig, que pertence à issue #218 (PR #235).
     */
    @Test
    @DisplayName("POST /entregas - Deve negar usuário sem ROLE_ALUNO (403 Forbidden)")
    void submeter_ComoProfessor_DeveRetornar403() throws Exception {
        comPrazoProjeto(hoje.plusDays(5));

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(URL_DOCUMENTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /entregas - Deve exigir token (401 Unauthorized)")
    void submeter_SemToken_DeveRetornar401() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(URL_DOCUMENTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /entregas - Deve identificar o aluno pelo token e ignorar identificadores do corpo")
    void submeter_ComAlunoIdNoCorpo_DeveUsarAlunoDoToken() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comPrazoProjeto(hoje.plusDays(5));
        Aluno outroAluno = alunoRepository.saveAndFlush(Aluno.builder()
                .nome("Outro Aluno")
                .email("outro@example.com")
                .curso("ADS")
                .build());

        enviar("""
                {
                  "tipo": "PROJETO_TCC",
                  "urlDocumento": "%s",
                  "alunoId": %d,
                  "orientadorId": 999
                }
                """.formatted(URL_DOCUMENTO, outroAluno.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aluno.email").value(EMAIL_ALUNO))
                .andExpect(jsonPath("$.orientador.email").value(EMAIL_ORIENTADOR));
    }

    @Test
    @DisplayName("POST /entregas - Deve rejeitar quando não há turma vigente (422 Unprocessable Content)")
    void submeter_SemTurmaVigente_DeveRetornar422() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);

        enviar(corpo(URL_DOCUMENTO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detalhes").value(
                        "Não há turma de TCC vigente cadastrada para receber entregas."));
    }

    @Test
    @DisplayName("POST /entregas - Deve rejeitar quando a etapa não tem prazo (422 Unprocessable Content)")
    void submeter_SemPrazo_DeveRetornar422() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comTurmaVigente();

        enviar(corpo(URL_DOCUMENTO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detalhes").value(
                        "Não há prazo cadastrado para Projeto de TCC na turma vigente."));
    }

    @Test
    @DisplayName("OPTIONS /entregas - Deve liberar o preflight de CORS do frontend sem exigir token")
    void preflight_DeOrigemPermitida_DeveRetornar200() throws Exception {
        mockMvc.perform(options(URL)
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }
}
