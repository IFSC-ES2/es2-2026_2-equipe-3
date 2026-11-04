package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.SubmissaoResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.GlobalExceptionHandler;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.service.SubmissaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de validação do endpoint POST /api/v1/submissoes (US06 / ADR-0013).
 * <p>
 * Cobre especificamente a exigência de protocolo HTTPS na URL do documento,
 * conforme o primeiro critério de aceitação da issue #218.
 */
@ExtendWith(MockitoExtension.class)
class SubmissaoControllerTest {

    private static final String EMAIL_ALUNO = "aluno@ifsc.edu.br";
    private static final String URL_ENDPOINT = "/api/v1/submissoes";

    private MockMvc mockMvc;

    @Mock
    private SubmissaoService submissaoService;

    @InjectMocks
    private SubmissaoController submissaoController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(submissaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor tokenAluno() {
        return request -> {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("email", EMAIL_ALUNO)
                    .build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
            return request;
        };
    }

    @Test
    @DisplayName("POST /submissoes - Deve retornar 201 Created ao enviar URL válida com https://")
    void criarSubmissao_DeveRetornar201_QuandoUrlHttpsValida() throws Exception {
        SubmissaoResponseDTO responseMock = new SubmissaoResponseDTO(
                1L, 10L, TipoEntrega.TCC1, "https://drive.google.com/file/d/abc123", Instant.now());

        when(submissaoService.criarSubmissao(any(), anyString())).thenReturn(responseMock);

        String requestBody = """
                {
                  "tipo": "TCC1",
                  "urlDocumento": "https://drive.google.com/file/d/abc123"
                }
                """;

        mockMvc.perform(post(URL_ENDPOINT)
                        .with(tokenAluno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.urlDocumento").value("https://drive.google.com/file/d/abc123"));
    }

    @ParameterizedTest(name = "URL inválida rejeitada: {0}")
    @DisplayName("POST /submissoes - Deve retornar 400 Bad Request para URLs sem https://, inválidas ou ausentes")
    @ValueSource(strings = {
            "http://drive.google.com/file/d/abc123",   // protocolo não seguro (http)
            "ftp://github.com/usuario/repositorio",     // protocolo diferente de https
            "drive.google.com/file/d/abc123",           // sem protocolo
            "nao-e-uma-url",                            // formato inválido
            ""                                          // em branco
    })
    void criarSubmissao_DeveRetornar400_QuandoUrlInvalida(String urlInvalida) throws Exception {
        String requestBody = """
                {
                  "tipo": "TCC1",
                  "urlDocumento": "%s"
                }
                """.formatted(urlInvalida);

        mockMvc.perform(post(URL_ENDPOINT)
                        .with(tokenAluno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(submissaoService, never()).criarSubmissao(any(), anyString());
    }

    @Test
    @DisplayName("POST /submissoes - Deve retornar 400 Bad Request quando a URL não é informada")
    void criarSubmissao_DeveRetornar400_QuandoUrlAusente() throws Exception {
        String requestBody = """
                {
                  "tipo": "TCC1"
                }
                """;

        mockMvc.perform(post(URL_ENDPOINT)
                        .with(tokenAluno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(submissaoService, never()).criarSubmissao(any(), anyString());
    }
}
