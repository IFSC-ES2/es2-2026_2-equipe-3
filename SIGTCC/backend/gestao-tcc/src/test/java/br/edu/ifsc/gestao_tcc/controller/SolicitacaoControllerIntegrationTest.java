package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitacaoControllerIntegrationTest {

    private static final String URL = "/api/v1/solicitacoes";
    private static final String EMAIL_ALUNO = "aluno@example.com";
    private static final String NOME_ALUNO = "Aluno de Teste";
    private static final String EMAIL_PROFESSOR_A = "professor.destinatario@ifsc.edu.br";
    private static final String EMAIL_PROFESSOR_B = "outro.professor@ifsc.edu.br";
    private static final String EMAIL_FORJADO = "outro.aluno@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    private Orientador professorA;
    private Orientador professorB;
    private SolicitacaoOrientacao solicitacaoDeProfessorA;

    @BeforeEach
    void setUp() {
        professorA = salvarProfessor("Professor Destinatário", EMAIL_PROFESSOR_A, 2);
        professorB = salvarProfessor("Outro Professor", EMAIL_PROFESSOR_B, 2);
        Aluno alunoExistente = salvarAluno("Aluno Existente PATCH", "aluno.patch@ifsc.edu.br");
        solicitacaoDeProfessorA = salvarSolicitacaoPendente(professorA, alunoExistente);
    }

    private Orientador salvarProfessor(String nome, String email, int vagas) {
        Orientador professor = new Orientador();
        professor.setNome(nome);
        professor.setEmail(email);
        professor.setDepartamento("DAELN");
        professor.setAtivo(true);
        professor.setPerfil(PerfilOrientador.builder()
                .vagasDisponiveis(vagas)
                .biografia("Biografia de " + nome)
                .orientador(professor)
                .build());
        return orientadorRepository.saveAndFlush(professor);
    }

    private Aluno salvarAluno(String nome, String email) {
        Aluno aluno = Aluno.builder()
                .nome(nome)
                .email(email)
                .curso("Análise e Desenvolvimento de Sistemas")
                .build();
        return alunoRepository.saveAndFlush(aluno);
    }

    private SolicitacaoOrientacao salvarSolicitacaoPendente(Orientador orientador, Aluno aluno) {
        SolicitacaoOrientacao solicitacao = SolicitacaoOrientacao.builder()
                .orientador(orientador)
                .aluno(aluno)
                .tema("Tema de TCC em Engenharia de Software")
                .mensagem("Gostaria de desenvolver meu trabalho sob sua orientação.")
                .status(StatusSolicitacao.PENDENTE)
                .build();
        return solicitacaoRepository.saveAndFlush(solicitacao);
    }

    private SolicitacaoOrientacao criarSolicitacaoParaAluno(String emailAluno) {
        Aluno aluno = salvarAluno("Aluno de Teste", emailAluno);

        return salvarSolicitacaoPendente(professorA, aluno);
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email).claim("name", NOME_ALUNO))
                .authorities(new SimpleGrantedAuthority(role));
    }

    private String payloadValido() {
        return """
                {
                  "orientadorId": %d,
                  "curso": "Engenharia de Software",
                  "tema": "Inteligência Artificial na Educação",
                  "mensagem": "Gostaria de ser orientado por você."
                }
                """.formatted(professorA.getId());
    }

    @Test
    @DisplayName("POST /solicitacoes - Aluno autenticado cria solicitação com o e-mail do token (201)")
    void criar_Aluno_DeveUsarEmailDoToken() throws Exception {
        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.aluno.email").value(EMAIL_ALUNO))
                .andExpect(jsonPath("$.aluno.nome").value(NOME_ALUNO))
                .andExpect(jsonPath("$.aluno.curso").value("Engenharia de Software"));

        assertThat(alunoRepository.findByEmail(EMAIL_ALUNO)).isPresent();
    }

    @Test
    @DisplayName("POST /solicitacoes - E-mail informado no corpo é ignorado")
    void criar_ComEmailNoCorpo_DeveIgnorarEUsarToken() throws Exception {
        String payload = """
                {
                  "orientadorId": %d,
                  "aluno": { "nome": "Outro", "email": "%s", "curso": "Outro curso" },
                  "curso": "Engenharia de Software",
                  "tema": "Inteligência Artificial na Educação",
                  "mensagem": "Gostaria de ser orientado por você."
                }
                """.formatted(professorA.getId(), EMAIL_FORJADO);

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aluno.email").value(EMAIL_ALUNO));

        assertThat(alunoRepository.findByEmail(EMAIL_FORJADO)).isEmpty();
    }

    @Test
    @DisplayName("POST /solicitacoes - Segunda solicitação pendente do mesmo aluno retorna 409")
    void criar_Duplicada_DeveRetornar409() throws Exception {
        long antes = solicitacaoRepository.count();

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido()))
                .andExpect(status().isCreated());

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido()))
                .andExpect(status().isConflict());

        assertThat(solicitacaoRepository.count()).isEqualTo(antes + 1);
    }

    @Test
    @DisplayName("POST /solicitacoes - Professor não pode criar solicitação (403)")
    void criar_Professor_DeveRetornar403() throws Exception {
        long antes = solicitacaoRepository.count();

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido()))
                .andExpect(status().isForbidden());

        assertThat(solicitacaoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("POST /solicitacoes - Sem token retorna 401")
    void criar_SemToken_DeveRetornar401() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /solicitacoes - Payload inválido retorna 400 com detalhes por campo")
    void criar_PayloadInvalido_DeveRetornar400() throws Exception {
        String payload = """
                {
                  "orientadorId": null,
                  "curso": "",
                  "tema": "IA",
                  "mensagem": ""
                }
                """;

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Aluno não pode aceitar solicitação (403 Forbidden)")
    void atualizarStatus_AlunoAceitando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch(URL + "/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andExpect(status().isForbidden());

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
        Orientador destinatario = orientadorRepository.findById(professorA.getId()).orElseThrow();
        assertThat(destinatario.getPerfil().getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Aluno não pode recusar solicitação (403 Forbidden)")
    void atualizarStatus_AlunoRecusando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch(URL + "/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RECUSADA\", \"justificativa\": \"Tentativa indevida pelo aluno.\"}"))
                .andExpect(status().isForbidden());

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
        Orientador destinatario = orientadorRepository.findById(professorA.getId()).orElseThrow();
        assertThat(destinatario.getPerfil().getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Outro professor não pode aceitar solicitação alheia (403 Forbidden)")
    void atualizarStatus_OutroProfessorAceitando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch(URL + "/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_B))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Somente o orientador destinatário pode aceitar ou recusar esta solicitação."));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
        Orientador destinatario = orientadorRepository.findById(professorA.getId()).orElseThrow();
        assertThat(destinatario.getPerfil().getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Outro professor não pode recusar solicitação alheia (403 Forbidden)")
    void atualizarStatus_OutroProfessorRecusando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch(URL + "/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_B))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RECUSADA\", \"justificativa\": \"Não tenho vagas neste semestre.\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
        Orientador destinatario = orientadorRepository.findById(professorA.getId()).orElseThrow();
        assertThat(destinatario.getPerfil().getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Orientador destinatário pode aceitar solicitação e decrementar vagas (200 OK)")
    void atualizarStatus_OrientadorDestinatarioAceitando_DeveRetornar200EDecrementarVagas() throws Exception {
        mockMvc.perform(patch(URL + "/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitacaoDeProfessorA.getId()))
                .andExpect(jsonPath("$.status").value("ACEITA"))
                .andExpect(jsonPath("$.orientador.vagasDisponiveis").value(1));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.ACEITA);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Orientador destinatário pode recusar solicitação com justificativa (200 OK)")
    void atualizarStatus_OrientadorDestinatarioRecusando_DeveRetornar200() throws Exception {
        mockMvc.perform(patch(URL + "/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RECUSADA\", \"justificativa\": \"Sem disponibilidade no momento.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitacaoDeProfessorA.getId()))
                .andExpect(jsonPath("$.status").value("RECUSADA"))
                .andExpect(jsonPath("$.justificativa").value("Sem disponibilidade no momento."));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.RECUSADA);
    }

    @Test
    @DisplayName("GET /solicitacoes/{id} - Aluno autor pode consultar sua solicitação (200)")
    void buscarPorId_AlunoAutor_DeveRetornar200() throws Exception {
        SolicitacaoOrientacao solicitacao =
                criarSolicitacaoParaAluno(EMAIL_ALUNO);

        mockMvc.perform(
                        get(URL + "/" + solicitacao.getId())
                                .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /solicitacoes/{id} - Usuário sem vínculo não pode consultar solicitação (403)")
    void buscarPorId_UsuarioSemVinculo_DeveRetornar403() throws Exception {
        SolicitacaoOrientacao solicitacao =
                criarSolicitacaoParaAluno(EMAIL_ALUNO);

        mockMvc.perform(
                        get(URL + "/" + solicitacao.getId())
                                .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_B))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Você não tem permissão para acessar esta solicitação."
                ));
    }

    @Test
    @DisplayName("GET /solicitacoes/{id} - Orientador responsável pode consultar solicitação (200)")
    void buscarPorId_OrientadorResponsavel_DeveRetornar200() throws Exception {
        SolicitacaoOrientacao solicitacao =
                criarSolicitacaoParaAluno(EMAIL_ALUNO);

        mockMvc.perform(
                        get(URL + "/" + solicitacao.getId())
                                .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /solicitacoes/orientador/{id} - Orientador responsável pode listar solicitações (200)")
    void listarPorOrientador_OrientadorResponsavel_DeveRetornar200() throws Exception {
        criarSolicitacaoParaAluno(EMAIL_ALUNO);

        mockMvc.perform(
                        get(URL + "/orientador/" + professorA.getId())
                                .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /solicitacoes/orientador/{id} - Outro professor não pode listar solicitações (403)")
    void listarPorOrientador_OutroProfessor_DeveRetornar403() throws Exception {
        criarSolicitacaoParaAluno(EMAIL_ALUNO);

        mockMvc.perform(
                        get(URL + "/orientador/" + professorA.getId())
                                .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_B))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Você não tem permissão para acessar as solicitações deste orientador."
                ));
    }

    @Test
    @DisplayName("GET /solicitacoes/{id} - Outro aluno não pode consultar solicitação de colega (403)")
    void buscarPorId_OutroAluno_DeveRetornar403() throws Exception {
        SolicitacaoOrientacao solicitacao =
                criarSolicitacaoParaAluno(EMAIL_ALUNO);

        mockMvc.perform(
                        get(URL + "/" + solicitacao.getId())
                                .with(tokenDe("ROLE_ALUNO", EMAIL_FORJADO))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Você não tem permissão para acessar esta solicitação."
                ));
    }

    @Test
    @DisplayName("GET /solicitacoes/orientador/{id} - Aluno não pode listar solicitações de orientador (403)")
    void listarPorOrientador_Aluno_DeveRetornar403() throws Exception {
        criarSolicitacaoParaAluno(EMAIL_ALUNO);

        mockMvc.perform(
                        get(URL + "/orientador/" + professorA.getId())
                                .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Você não tem permissão para acessar as solicitações deste orientador."
                ));
    }
}
