package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitacaoControllerIntegrationTest {

    private static final String URL = "/api/v1/solicitacoes";
    private static final String EMAIL_ALUNO = "aluno@example.com";
    private static final String NOME_ALUNO = "Aluno de Teste";
    private static final String EMAIL_PROFESSOR = "professor.orientador@example.com";
    private static final String EMAIL_FORJADO = "outro.aluno@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    private Orientador orientador;

    @BeforeEach
    void setUp() {
        Orientador professor = new Orientador();
        professor.setNome("Professor Orientador");
        professor.setEmail(EMAIL_PROFESSOR);
        professor.setDepartamento("DAELN");
        professor.setAtivo(true);
        professor.setPerfil(PerfilOrientador.builder()
                .vagasDisponiveis(3)
                .biografia("Biografia")
                .orientador(professor)
                .build());
        orientador = orientadorRepository.saveAndFlush(professor);
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
                """.formatted(orientador.getId());
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
                """.formatted(orientador.getId(), EMAIL_FORJADO);

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

        assertThat(solicitacaoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /solicitacoes - Professor não pode criar solicitação (403)")
    void criar_Professor_DeveRetornar403() throws Exception {
        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadValido()))
                .andExpect(status().isForbidden());

        assertThat(solicitacaoRepository.count()).isZero();
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
}
