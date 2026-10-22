package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.config.ClockConfig;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrazoEntregaControllerIntegrationTest {

    private static final String URL = "/api/v1/prazos";
    private static final String EMAIL_RESPONSAVEL = "professor.responsavel@example.com";
    private static final String EMAIL_ORIENTADOR = "professor.orientador@example.com";
    private static final String EMAIL_ALUNO = "aluno@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private TurmaTCCRepository turmaTCCRepository;

    @Autowired
    private PrazoEntregaRepository prazoEntregaRepository;

    private TurmaTCC turmaVigente;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        hoje = LocalDate.now(ClockConfig.FUSO_INSTITUICAO);

        Orientador responsavel = salvarProfessor("Professor Responsável", EMAIL_RESPONSAVEL);
        salvarProfessor("Professor Orientador", EMAIL_ORIENTADOR);

        turmaVigente = turmaTCCRepository.saveAndFlush(TurmaTCC.builder()
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(responsavel)
                .build());
    }

    private Orientador salvarProfessor(String nome, String email) {
        Orientador professor = new Orientador();
        professor.setNome(nome);
        professor.setEmail(email);
        professor.setDepartamento("DAELN");
        professor.setAtivo(true);
        return orientadorRepository.saveAndFlush(professor);
    }

    private PrazoEntrega salvarPrazo(TipoEntrega tipo, LocalDate dataLimite) {
        return prazoEntregaRepository.saveAndFlush(PrazoEntrega.builder()
                .turma(turmaVigente)
                .tipo(tipo)
                .dataLimite(dataLimite)
                .build());
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email))
                .authorities(new SimpleGrantedAuthority(role));
    }

    @Test
    @DisplayName("POST /prazos - Deve cadastrar prazo pelo professor responsável pela turma vigente (201 Created)")
    void cadastraPrazo_ProfessorResponsavel_DeveRetornar201() throws Exception {
        LocalDate dataProjeto = hoje.plusDays(15);
        String payload = """
                {
                  "tipo": "PROJETO_TCC",
                  "dataLimite": "%s"
                }
                """.formatted(dataProjeto);

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_RESPONSAVEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/prazos/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.semestre").value("2026/2"))
                .andExpect(jsonPath("$.tipo").value("PROJETO_TCC"))
                .andExpect(jsonPath("$.descricao").value("Projeto de TCC"))
                .andExpect(jsonPath("$.dataLimite").value(dataProjeto.toString()));
    }

    @Test
    @DisplayName("POST e PATCH /prazos - Deve bloquear cadastro e edição por professor não responsável pela turma (403 Forbidden)")
    void gerenciaPrazo_ProfessorNaoResponsavel_DeveRetornar403() throws Exception {
        LocalDate dataProjeto = hoje.plusDays(15);
        PrazoEntrega existente = salvarPrazo(TipoEntrega.PROJETO_TCC, dataProjeto);

        String payloadCadastro = """
                {
                  "tipo": "TCC1",
                  "dataLimite": "%s"
                }
                """.formatted(hoje.plusDays(30));

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));

        String payloadEdicao = """
                {
                  "dataLimite": "%s"
                }
                """.formatted(hoje.plusDays(20));

        mockMvc.perform(patch(URL + "/" + existente.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadEdicao))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    @DisplayName("POST /prazos - Deve cadastrar os três tipos de entrega (Projeto de TCC, TCC 1, TCC 2) em sequência")
    void cadastraPrazo_TresTiposDeEntrega_DeveCadastrarTodosComSucesso() throws Exception {
        LocalDate dataProjeto = hoje.plusDays(10);
        LocalDate dataTcc1 = hoje.plusDays(30);
        LocalDate dataTcc2 = hoje.plusDays(60);

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_RESPONSAVEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "tipo": "PROJETO_TCC", "dataLimite": "%s" }
                                """.formatted(dataProjeto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("PROJETO_TCC"))
                .andExpect(jsonPath("$.descricao").value("Projeto de TCC"));

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_RESPONSAVEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "tipo": "TCC1", "dataLimite": "%s" }
                                """.formatted(dataTcc1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("TCC1"))
                .andExpect(jsonPath("$.descricao").value("TCC 1"));

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_RESPONSAVEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "tipo": "TCC2", "dataLimite": "%s" }
                                """.formatted(dataTcc2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("TCC2"))
                .andExpect(jsonPath("$.descricao").value("TCC 2"));
    }

    @Test
    @DisplayName("PATCH /prazos/{id} - Deve editar prazo já cadastrado pelo professor responsável (200 OK)")
    void atualizaPrazo_PrazoExistente_DeveAtualizarDataLimite() throws Exception {
        PrazoEntrega prazo = salvarPrazo(TipoEntrega.PROJETO_TCC, hoje.plusDays(10));
        LocalDate novaData = hoje.plusDays(20);

        String payload = """
                {
                  "dataLimite": "%s"
                }
                """.formatted(novaData);

        mockMvc.perform(patch(URL + "/" + prazo.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_RESPONSAVEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prazo.getId()))
                .andExpect(jsonPath("$.tipo").value("PROJETO_TCC"))
                .andExpect(jsonPath("$.dataLimite").value(novaData.toString()));
    }

    @Test
    @DisplayName("POST e PATCH /prazos - Deve rejeitar datas inválidas (no passado ou fora de ordem cronológica)")
    void gerenciaPrazo_DataInvalida_DeveRejeitarCom400Ou422() throws Exception {
        // 1. Data no passado -> 400 Bad Request
        LocalDate ontem = hoje.minusDays(1);
        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_RESPONSAVEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "tipo": "PROJETO_TCC", "dataLimite": "%s" }
                                """.formatted(ontem)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalhes[0].campo").value("dataLimite"))
                .andExpect(jsonPath("$.detalhes[0].mensagem").value("A data limite não pode estar no passado."));

        // 2. Violação cronológica (TCC 1 antes de Projeto de TCC) -> 422 Unprocessable Content
        salvarPrazo(TipoEntrega.PROJETO_TCC, hoje.plusDays(20));

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_RESPONSAVEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "tipo": "TCC1", "dataLimite": "%s" }
                                """.formatted(hoje.plusDays(10))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.erro").value("Regra de negócio violada"));
    }

    @Test
    @DisplayName("GET /prazos - Deve permitir ao Aluno visualizar os prazos vigentes ordenados por etapa (200 OK)")
    void listaPrazosVigentes_AutenticadoComoAluno_DeveRetornarPrazosOrdenados() throws Exception {
        LocalDate dataProjeto = hoje.plusDays(10);
        LocalDate dataTcc1 = hoje.plusDays(30);
        LocalDate dataTcc2 = hoje.plusDays(60);

        salvarPrazo(TipoEntrega.TCC2, dataTcc2);
        salvarPrazo(TipoEntrega.PROJETO_TCC, dataProjeto);
        salvarPrazo(TipoEntrega.TCC1, dataTcc1);

        mockMvc.perform(get(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].tipo").value("PROJETO_TCC"))
                .andExpect(jsonPath("$[0].dataLimite").value(dataProjeto.toString()))
                .andExpect(jsonPath("$[1].tipo").value("TCC1"))
                .andExpect(jsonPath("$[1].dataLimite").value(dataTcc1.toString()))
                .andExpect(jsonPath("$[2].tipo").value("TCC2"))
                .andExpect(jsonPath("$[2].dataLimite").value(dataTcc2.toString()));
    }
}