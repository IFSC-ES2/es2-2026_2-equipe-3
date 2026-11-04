package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.SubmissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração do endpoint POST /api/v1/submissoes (US06 / ADR-0013),
 * cobrindo persistência, validação de URL e o fluxo de CORS exigido pelos
 * critérios de aceitação da issue #218.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubmissaoControllerIntegrationTest {

    private static final String URL = "/api/v1/submissoes";
    private static final String EMAIL_ALUNO = "aluno.submissao@ifsc.edu.br";
    private static final String ORIGEM_FRONT_DEV = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private SubmissaoRepository submissaoRepository;

    @BeforeEach
    void setUp() {
        Aluno aluno = Aluno.builder()
                .nome("Aluno de Teste")
                .email(EMAIL_ALUNO)
                .curso("Engenharia de Software")
                .build();
        alunoRepository.saveAndFlush(aluno);
    }

    private RequestPostProcessor tokenAluno() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", EMAIL_ALUNO))
                .authorities(new SimpleGrantedAuthority("ROLE_ALUNO"));
    }

    @Test
    @DisplayName("POST /submissoes - Persiste a submissão quando a URL é https:// válida")
    void criarSubmissao_DevePersistir_QuandoUrlHttpsValida() throws Exception {
        String requestBody = """
                {
                  "tipo": "TCC1",
                  "urlDocumento": "https://github.com/aluno/trabalho-tcc"
                }
                """;

        mockMvc.perform(post(URL)
                        .with(tokenAluno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.urlDocumento").value("https://github.com/aluno/trabalho-tcc"))
                .andExpect(jsonPath("$.tipo").value("TCC1"));

        assertThat(submissaoRepository.findByAlunoIdOrderByEnviadoEmDesc(
                alunoRepository.findByEmail(EMAIL_ALUNO).orElseThrow().getId())).hasSize(1);
    }

    @Test
    @DisplayName("POST /submissoes - Rejeita (400) e não persiste quando a URL usa http:// em vez de https://")
    void criarSubmissao_DeveRejeitar_QuandoUrlHttp() throws Exception {
        String requestBody = """
                {
                  "tipo": "TCC1",
                  "urlDocumento": "http://github.com/aluno/trabalho-tcc"
                }
                """;

        mockMvc.perform(post(URL)
                        .with(tokenAluno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0].campo").value("urlDocumento"));

        assertThat(submissaoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /submissoes - Requisição JSON autenticada a partir da origem do frontend passa pelo CORS sem erro")
    void criarSubmissao_DevePassarPeloCors_QuandoOrigemPermitida() throws Exception {
        String requestBody = """
                {
                  "tipo": "PROJETO_TCC",
                  "urlDocumento": "https://drive.google.com/file/d/xyz"
                }
                """;

        mockMvc.perform(post(URL)
                        .with(tokenAluno())
                        .header(HttpHeaders.ORIGIN, ORIGEM_FRONT_DEV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_FRONT_DEV));
    }

    @Test
    @DisplayName("OPTIONS /submissoes - Preflight de CORS é respondido corretamente para a origem do frontend")
    void preflightSubmissoes_DeveSerPermitido_ParaOrigemDoFrontend() throws Exception {
        mockMvc.perform(options(URL)
                        .header(HttpHeaders.ORIGIN, ORIGEM_FRONT_DEV)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_FRONT_DEV));
    }
}
