package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.config.ClockConfig;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Entrega;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.Parecer;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    private static final String EMAIL_OUTRO_ALUNO = "outro.aluno@example.com";
    private static final String EMAIL_OUTRO_ORIENTADOR = "outro.professor@example.com";
    private static final String URL_DOCUMENTO = "https://docs.google.com/document/d/123/edit?usp=sharing";
    private static final String NOVA_URL = "https://github.com/org/tcc";
    private static final Instant ENVIO_ANTERIOR = Instant.parse("2026-01-10T10:00:00Z");
    private static final String COMENTARIO_PARECER = "Detalhar melhor a seção de metodologia.";
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

    @Autowired
    private ParecerRepository parecerRepository;

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

        Parecer parecerAnterior = parecerRepository.saveAndFlush(Parecer.builder()
                .entrega(existente)
                .orientador(orientador)
                .resultado(StatusEntrega.AJUSTES_NECESSARIOS)
                .comentario(COMENTARIO_PARECER)
                .build());

        enviar(corpo(NOVA_URL))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").value(existente.getId()))
                .andExpect(jsonPath("$.urlDocumento").value(NOVA_URL))
                .andExpect(jsonPath("$.status").value("SUBMETIDO"))
                .andExpect(jsonPath("$.atrasada").value(true))
                .andExpect(jsonPath("$.parecer.resultado").value("AJUSTES_NECESSARIOS"))
                .andExpect(jsonPath("$.parecer.comentario").value(COMENTARIO_PARECER));

        // Descarta o cache da sessão para ler o que realmente foi gravado no banco.
        entityManager.flush();
        entityManager.clear();

        List<Parecer> historico = parecerRepository
                .findByEntregaIdOrderByRegistradoEmDesc(existente.getId());

        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getId()).isEqualTo(parecerAnterior.getId());
        assertThat(historico.getFirst().getResultado())
                .isEqualTo(StatusEntrega.AJUSTES_NECESSARIOS);
        assertThat(historico.getFirst().getComentario())
                .isEqualTo(COMENTARIO_PARECER);

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

    private Entrega comEntregaDe(Aluno autor, Orientador responsavel, TurmaTCC turma, TipoEntrega tipo,
                                 StatusEntrega status, String enviadoEm) {
        return entregaRepository.saveAndFlush(Entrega.builder()
                .aluno(autor)
                .orientador(responsavel)
                .turma(turma)
                .tipo(tipo)
                .status(status)
                .urlDocumento(URL_DOCUMENTO)
                .atrasada(true)
                .enviadoEm(Instant.parse(enviadoEm))
                .build());
    }

    private Aluno comOutroAluno() {
        return alunoRepository.saveAndFlush(Aluno.builder()
                .nome("Outro Aluno")
                .email(EMAIL_OUTRO_ALUNO)
                .curso("ADS")
                .build());
    }

    private Orientador comOutroOrientador() {
        Orientador outro = new Orientador();
        outro.setNome("Outro Professor");
        outro.setEmail(EMAIL_OUTRO_ORIENTADOR);
        outro.setDepartamento("DAMM");
        outro.setAtivo(true);
        return orientadorRepository.saveAndFlush(outro);
    }

    @ParameterizedTest
    @CsvSource({"ROLE_PROFESSOR," + EMAIL_ORIENTADOR, "ROLE_ALUNO," + EMAIL_ALUNO})
    @DisplayName("GET /entregas/{id} - Deve retornar a entrega ao orientador vinculado e ao aluno autor (200 OK)")
    void buscarPorId_ComVinculo_DeveRetornar200(String role, String email) throws Exception {
        Entrega entrega = comEntregaDe(aluno, orientador, comTurmaVigente(), TipoEntrega.PROJETO_TCC,
                StatusEntrega.SUBMETIDO, "2026-11-03T14:20:00Z");

        mockMvc.perform(get(URL + "/" + entrega.getId()).with(tokenDe(role, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entrega.getId()))
                .andExpect(jsonPath("$.semestre").value("2026/2"))
                .andExpect(jsonPath("$.tipo").value("PROJETO_TCC"))
                .andExpect(jsonPath("$.descricaoTipo").value("Projeto de TCC"))
                .andExpect(jsonPath("$.urlDocumento").value(URL_DOCUMENTO))
                .andExpect(jsonPath("$.status").value("SUBMETIDO"))
                .andExpect(jsonPath("$.atrasada").value(true))
                .andExpect(jsonPath("$.enviadoEm").value("2026-11-03T14:20:00Z"))
                .andExpect(jsonPath("$.aluno.id").value(aluno.getId()))
                .andExpect(jsonPath("$.aluno.nome").value("Aluno de Teste"))
                .andExpect(jsonPath("$.aluno.email").value(EMAIL_ALUNO))
                .andExpect(jsonPath("$.aluno.curso").value("Análise e Desenvolvimento de Sistemas"))
                .andExpect(jsonPath("$.orientador.id").value(orientador.getId()))
                .andExpect(jsonPath("$.orientador.nome").value("Professor Orientador"))
                .andExpect(jsonPath("$.orientador.email").value(EMAIL_ORIENTADOR))
                .andExpect(jsonPath("$.parecer").value(nullValue()));
    }

    @ParameterizedTest
    @CsvSource({"ROLE_PROFESSOR," + EMAIL_OUTRO_ORIENTADOR, "ROLE_ALUNO," + EMAIL_OUTRO_ALUNO})
    @DisplayName("GET /entregas/{id} - Deve negar professor não vinculado e outro aluno, com corpo (403 Forbidden)")
    void buscarPorId_SemVinculo_DeveRetornar403ComCorpo(String role, String email) throws Exception {
        Entrega entrega = comEntregaDe(aluno, orientador, comTurmaVigente(), TipoEntrega.PROJETO_TCC,
                StatusEntrega.SUBMETIDO, "2026-11-03T14:20:00Z");

        mockMvc.perform(get(URL + "/" + entrega.getId()).with(tokenDe(role, email)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.caminho").value(URL + "/" + entrega.getId()))
                .andExpect(jsonPath("$.detalhes").value(
                        "Acesso permitido apenas ao aluno autor ou ao orientador vinculado a esta entrega."));
    }

    @Test
    @DisplayName("GET /entregas/{id} - Deve negar token sem role de aluno ou professor, sem corpo (403 Forbidden)")
    void buscarPorId_SemRole_DeveRetornar403SemCorpo() throws Exception {
        mockMvc.perform(get(URL + "/1").with(tokenDe("ROLE_OUTRO", EMAIL_ORIENTADOR)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /entregas/{id} - Deve informar entrega inexistente (404 Not Found)")
    void buscarPorId_Inexistente_DeveRetornar404() throws Exception {
        mockMvc.perform(get(URL + "/99").with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detalhes").value("Entrega com identificador 99 não foi encontrada."));
    }

    @Test
    @DisplayName("GET /entregas/{id} - Deve rejeitar identificador não numérico (400 Bad Request)")
    void buscarPorId_IdInvalido_DeveRetornar400() throws Exception {
        mockMvc.perform(get(URL + "/abc").with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro de requisição inválido"))
                .andExpect(jsonPath("$.detalhes").value("Valor inválido para o parâmetro 'id'."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/1", "/orientador", "/minhas"})
    @DisplayName("GET /entregas/* - Deve exigir token (401 Unauthorized)")
    void consultas_SemToken_DevemRetornar401(String rota) throws Exception {
        mockMvc.perform(get(URL + rota))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /entregas/{id} - O Location devolvido pelo POST deve levar à entrega criada")
    void buscarPorId_PeloLocationDoPost_DeveRetornarEntrega() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comPrazoProjeto(hoje.plusDays(5));

        String location = enviar(corpo(URL_DOCUMENTO))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.LOCATION);

        mockMvc.perform(get(URI.create(location)).with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlDocumento").value(URL_DOCUMENTO));
    }

    @Test
    @DisplayName("GET /entregas/orientador - Deve listar só os orientandos, da mais recente para a mais antiga (200 OK)")
    void listarDoOrientador_DeveRetornarEntregasOrdenadas() throws Exception {
        TurmaTCC turma = comTurmaVigente();
        Entrega antiga = comEntregaDe(aluno, orientador, turma, TipoEntrega.PROJETO_TCC,
                StatusEntrega.APROVADO, "2026-10-01T10:00:00Z");
        Entrega recente = comEntregaDe(aluno, orientador, turma, TipoEntrega.TCC1,
                StatusEntrega.SUBMETIDO, "2026-11-01T10:00:00Z");
        comEntregaDe(comOutroAluno(), comOutroOrientador(), turma, TipoEntrega.PROJETO_TCC,
                StatusEntrega.SUBMETIDO, "2026-11-02T10:00:00Z");

        mockMvc.perform(get(URL + "/orientador").with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(recente.getId()))
                .andExpect(jsonPath("$[0].urlDocumento").value(URL_DOCUMENTO))
                .andExpect(jsonPath("$[1].id").value(antiga.getId()));
    }

    @Test
    @DisplayName("GET /entregas/orientador?status - Deve filtrar pelo status informado (200 OK)")
    void listarDoOrientador_ComStatus_DeveFiltrar() throws Exception {
        TurmaTCC turma = comTurmaVigente();
        comEntregaDe(aluno, orientador, turma, TipoEntrega.PROJETO_TCC,
                StatusEntrega.APROVADO, "2026-10-01T10:00:00Z");
        Entrega submetida = comEntregaDe(aluno, orientador, turma, TipoEntrega.TCC1,
                StatusEntrega.SUBMETIDO, "2026-11-01T10:00:00Z");

        mockMvc.perform(get(URL + "/orientador").param("status", "SUBMETIDO")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(submetida.getId()));
    }

    @Test
    @DisplayName("GET /entregas/orientador - Deve retornar lista vazia sem entregas (200 OK)")
    void listarDoOrientador_SemEntregas_DeveRetornarVazio() throws Exception {
        mockMvc.perform(get(URL + "/orientador").with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /entregas/orientador - Deve rejeitar status fora da enumeração (400 Bad Request)")
    void listarDoOrientador_StatusInvalido_DeveRetornar400() throws Exception {
        mockMvc.perform(get(URL + "/orientador").param("status", "INVALIDO")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro de requisição inválido"))
                .andExpect(jsonPath("$.detalhes").value("Valor inválido para o parâmetro 'status'. "
                        + "Valores aceitos: SUBMETIDO, AJUSTES_NECESSARIOS, APROVADO, REPROVADO."));
    }

    @Test
    @DisplayName("GET /entregas/orientador - Deve informar orientador não cadastrado (404 Not Found)")
    void listarDoOrientador_Desconhecido_DeveRetornar404() throws Exception {
        mockMvc.perform(get(URL + "/orientador").with(tokenDe("ROLE_PROFESSOR", "desconhecido@example.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Orientador com e-mail desconhecido@example.com não foi encontrado."));
    }

    @Test
    @DisplayName("GET /entregas/orientador - Deve negar usuário sem ROLE_PROFESSOR, sem corpo (403 Forbidden)")
    void listarDoOrientador_ComoAluno_DeveRetornar403SemCorpo() throws Exception {
        mockMvc.perform(get(URL + "/orientador").with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /entregas/minhas - Deve listar só as entregas do aluno, ordenadas por etapa (200 OK)")
    void listarMinhas_DeveRetornarEntregasOrdenadasPorEtapa() throws Exception {
        TurmaTCC turma = comTurmaVigente();
        comEntregaDe(aluno, orientador, turma, TipoEntrega.TCC2, StatusEntrega.SUBMETIDO, "2026-11-01T10:00:00Z");
        comEntregaDe(aluno, orientador, turma, TipoEntrega.PROJETO_TCC, StatusEntrega.APROVADO, "2026-09-01T10:00:00Z");
        comEntregaDe(aluno, orientador, turma, TipoEntrega.TCC1, StatusEntrega.APROVADO, "2026-10-01T10:00:00Z");
        comEntregaDe(comOutroAluno(), orientador, turma, TipoEntrega.PROJETO_TCC,
                StatusEntrega.SUBMETIDO, "2026-11-02T10:00:00Z");

        mockMvc.perform(get(URL + "/minhas").with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].tipo").value("PROJETO_TCC"))
                .andExpect(jsonPath("$[1].tipo").value("TCC1"))
                .andExpect(jsonPath("$[2].tipo").value("TCC2"));
    }

    @Test
    @DisplayName("GET /entregas/minhas - Deve retornar lista vazia para aluno sem cadastro (200 OK)")
    void listarMinhas_AlunoSemCadastro_DeveRetornarVazio() throws Exception {
        mockMvc.perform(get(URL + "/minhas").with(tokenDe("ROLE_ALUNO", "sem.cadastro@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /entregas/minhas - Deve negar usuário sem ROLE_ALUNO, sem corpo (403 Forbidden)")
    void listarMinhas_ComoProfessor_DeveRetornar403SemCorpo() throws Exception {
        mockMvc.perform(get(URL + "/minhas").with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    private ResultActions registrarParecer(Long id, String payload) throws Exception {
        return mockMvc.perform(post(URL + "/" + id + "/parecer")
                .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    private String corpoParecer(String resultado, String comentario) {
        return """
                {
                  "resultado": "%s",
                  "comentario": "%s"
                }
                """.formatted(resultado, comentario);
    }

    private String corpoParecerSemComentario(String resultado) {
        return """
                { "resultado": "%s" }
                """.formatted(resultado);
    }

    private Entrega comEntregaSubmetida() {
        return comEntrega(comTurmaVigente(), StatusEntrega.SUBMETIDO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"APROVADO", "AJUSTES_NECESSARIOS", "REPROVADO"})
    @DisplayName("POST /entregas/{id}/parecer - Deve gravar o parecer e atualizar o status (200 OK)")
    void registrarParecer_ResultadoValido_DeveRetornar200EPersistir(String resultado) throws Exception {
        Entrega entrega = comEntregaSubmetida();

        registrarParecer(entrega.getId(), corpoParecer(resultado, COMENTARIO_PARECER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entrega.getId()))
                .andExpect(jsonPath("$.status").value(resultado))
                .andExpect(jsonPath("$.parecer.id").isNumber())
                .andExpect(jsonPath("$.parecer.resultado").value(resultado))
                .andExpect(jsonPath("$.parecer.comentario").value(COMENTARIO_PARECER))
                .andExpect(jsonPath("$.parecer.registradoEm").isNotEmpty());

        // Descarta o cache da sessão para ler o que realmente foi gravado no banco.
        entityManager.flush();
        entityManager.clear();

        Entrega gravada = entregaRepository.findById(entrega.getId()).orElseThrow();
        assertThat(gravada.getStatus()).isEqualTo(StatusEntrega.valueOf(resultado));
        assertThat(gravada.getEnviadoEm()).isEqualTo(ENVIO_ANTERIOR);

        Parecer parecer = parecerRepository.findFirstByEntregaIdOrderByRegistradoEmDescIdDesc(entrega.getId())
                .orElseThrow();
        assertThat(parecer.getResultado()).isEqualTo(StatusEntrega.valueOf(resultado));
        assertThat(parecer.getComentario()).isEqualTo(COMENTARIO_PARECER);
        assertThat(parecer.getOrientador().getId()).isEqualTo(orientador.getId());
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve aceitar aprovação sem comentário (200 OK)")
    void registrarParecer_AprovadoSemComentario_DeveRetornar200() throws Exception {
        Entrega entrega = comEntregaSubmetida();

        registrarParecer(entrega.getId(), corpoParecerSemComentario("APROVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.parecer.comentario").value(nullValue()));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve percorrer ajustes, reenvio e aprovação expondo o parecer mais recente")
    void registrarParecer_FluxoCompleto_DeveExporParecerMaisRecente() throws Exception {
        comSolicitacao(StatusSolicitacao.ACEITA);
        comPrazoProjeto(hoje.plusDays(5));
        enviar(corpo(URL_DOCUMENTO)).andExpect(status().isCreated());
        Long id = entregaRepository.findByAlunoIdOrderByTipoAsc(aluno.getId()).getFirst().getId();

        registrarParecer(id, corpoParecer("AJUSTES_NECESSARIOS", COMENTARIO_PARECER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AJUSTES_NECESSARIOS"));

        enviar(corpo(NOVA_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMETIDO"))
                .andExpect(jsonPath("$.urlDocumento").value(NOVA_URL))
                .andExpect(jsonPath("$.parecer.resultado").value("AJUSTES_NECESSARIOS"))
                .andExpect(jsonPath("$.parecer.comentario").value(COMENTARIO_PARECER));

        registrarParecer(id, corpoParecer("APROVADO", "Ajustes atendidos."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.parecer.resultado").value("APROVADO"));

        registrarParecer(id, corpoParecer("REPROVADO", COMENTARIO_PARECER))
                .andExpect(status().isConflict());

        // Os dois pareceres são registrados no mesmo segundo, então isto também exercita o desempate por id.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get(URL + "/" + id).with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.parecer.resultado").value("APROVADO"))
                .andExpect(jsonPath("$.parecer.comentario").value("Ajustes atendidos."));
    }

    @ParameterizedTest
    @CsvSource({
            "/{id},ROLE_PROFESSOR," + EMAIL_ORIENTADOR + ",$.parecer.resultado",
            "/{id},ROLE_ALUNO," + EMAIL_ALUNO + ",$.parecer.resultado",
            "/orientador,ROLE_PROFESSOR," + EMAIL_ORIENTADOR + ",$[0].parecer.resultado",
            "/minhas,ROLE_ALUNO," + EMAIL_ALUNO + ",$[0].parecer.resultado"
    })
    @DisplayName("GET /entregas/* - Deve expor o parecer registrado nas consultas (200 OK)")
    void consultas_ComParecer_DevemExporParecer(String rota, String role, String email, String caminho)
            throws Exception {
        Entrega entrega = comEntregaSubmetida();
        registrarParecer(entrega.getId(), corpoParecer("REPROVADO", COMENTARIO_PARECER))
                .andExpect(status().isOk());

        mockMvc.perform(get(URL + rota.replace("{id}", entrega.getId().toString())).with(tokenDe(role, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(caminho).value("REPROVADO"));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve rejeitar resultado ausente (400 Bad Request)")
    void registrarParecer_ResultadoAusente_DeveRetornar400() throws Exception {
        registrarParecer(1L, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação nos dados enviados"))
                .andExpect(jsonPath("$.detalhes[0].campo").value("resultado"))
                .andExpect(jsonPath("$.detalhes[0].mensagem").value("O resultado do parecer é obrigatório."));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve rejeitar resultado fora da enumeração (400 Bad Request)")
    void registrarParecer_ResultadoInvalido_DeveRetornar400() throws Exception {
        registrarParecer(1L, corpoParecerSemComentario("RESSALVAS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido ou ilegível"))
                .andExpect(jsonPath("$.detalhes").value("O corpo da requisição está ausente, mal formatado "
                        + "ou contém um valor incompatível com o tipo esperado."));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve rejeitar comentário com mais de 2000 caracteres (400 Bad Request)")
    void registrarParecer_ComentarioMuitoLongo_DeveRetornar400() throws Exception {
        registrarParecer(1L, corpoParecer("APROVADO", "a".repeat(2001)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0].campo").value("comentario"))
                .andExpect(jsonPath("$.detalhes[0].mensagem").value("O comentário deve ter no máximo 2000 caracteres."));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve aceitar comentário com exatamente 2000 caracteres (200 OK)")
    void registrarParecer_ComentarioNoLimite_DeveRetornar200() throws Exception {
        Entrega entrega = comEntregaSubmetida();

        registrarParecer(entrega.getId(), corpoParecer("APROVADO", "a".repeat(2000)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve rejeitar identificador não numérico (400 Bad Request)")
    void registrarParecer_IdInvalido_DeveRetornar400() throws Exception {
        mockMvc.perform(post(URL + "/abc/parecer")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoParecerSemComentario("APROVADO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro de requisição inválido"))
                .andExpect(jsonPath("$.detalhes").value("Valor inválido para o parâmetro 'id'."));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve negar professor não vinculado, com corpo (403 Forbidden)")
    void registrarParecer_ProfessorNaoVinculado_DeveRetornar403ComCorpo() throws Exception {
        Entrega entrega = comEntregaSubmetida();
        String caminho = URL + "/" + entrega.getId() + "/parecer";

        mockMvc.perform(post(caminho)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_OUTRO_ORIENTADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoParecerSemComentario("APROVADO")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.caminho").value(caminho))
                .andExpect(jsonPath("$.detalhes").value(
                        "Somente o orientador vinculado à entrega pode registrar parecer."));

        assertThat(parecerRepository.findByEntregaIdOrderByRegistradoEmDesc(entrega.getId())).isEmpty();
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve negar aluno, sem corpo (403 Forbidden)")
    void registrarParecer_ComoAluno_DeveRetornar403SemCorpo() throws Exception {
        Entrega entrega = comEntregaSubmetida();

        mockMvc.perform(post(URL + "/" + entrega.getId() + "/parecer")
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoParecerSemComentario("APROVADO")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve exigir token (401 Unauthorized)")
    void registrarParecer_SemToken_DeveRetornar401() throws Exception {
        mockMvc.perform(post(URL + "/1/parecer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoParecerSemComentario("APROVADO")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve informar entrega inexistente (404 Not Found)")
    void registrarParecer_EntregaInexistente_DeveRetornar404() throws Exception {
        registrarParecer(99L, corpoParecerSemComentario("APROVADO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detalhes").value("Entrega com identificador 99 não foi encontrada."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"APROVADO", "REPROVADO"})
    @DisplayName("POST /entregas/{id}/parecer - Deve bloquear entrega com parecer definitivo (409 Conflict)")
    void registrarParecer_ComParecerDefinitivo_DeveRetornar409(String statusAtual) throws Exception {
        Entrega entrega = comEntrega(comTurmaVigente(), StatusEntrega.valueOf(statusAtual));

        registrarParecer(entrega.getId(), corpoParecer("AJUSTES_NECESSARIOS", COMENTARIO_PARECER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Conflito de dados"))
                .andExpect(jsonPath("$.detalhes").value("Esta entrega já possui parecer definitivo ("
                        + statusAtual + ") e não pode ser reavaliada."));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve bloquear entrega aguardando reenvio (409 Conflict)")
    void registrarParecer_AguardandoReenvio_DeveRetornar409() throws Exception {
        Entrega entrega = comEntrega(comTurmaVigente(), StatusEntrega.AJUSTES_NECESSARIOS);

        registrarParecer(entrega.getId(), corpoParecerSemComentario("APROVADO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalhes").value(
                        "Esta entrega aguarda reenvio do aluno antes de receber um novo parecer."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AJUSTES_NECESSARIOS", "REPROVADO"})
    @DisplayName("POST /entregas/{id}/parecer - Deve exigir comentário em ajustes e reprovação (422 Unprocessable Content)")
    void registrarParecer_SemComentarioObrigatorio_DeveRetornar422(String resultado) throws Exception {
        Entrega entrega = comEntregaSubmetida();
        String mensagem = "O comentário com a justificativa é obrigatório "
                + "quando o parecer exige ajustes ou reprova a entrega.";

        registrarParecer(entrega.getId(), corpoParecerSemComentario(resultado))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erro").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.detalhes").value(mensagem));

        registrarParecer(entrega.getId(), corpoParecer(resultado, "   "))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detalhes").value(mensagem));
    }

    @Test
    @DisplayName("POST /entregas/{id}/parecer - Deve rejeitar SUBMETIDO como resultado (422 Unprocessable Content)")
    void registrarParecer_ResultadoSubmetido_DeveRetornar422() throws Exception {
        Entrega entrega = comEntregaSubmetida();

        registrarParecer(entrega.getId(), corpoParecer("SUBMETIDO", COMENTARIO_PARECER))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detalhes").value(
                        "O resultado do parecer deve ser APROVADO, AJUSTES_NECESSARIOS ou REPROVADO."));
    }
}
