package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.AtualizaStatusRequest;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.AcessoNegadoException;
import br.edu.ifsc.gestao_tcc.exception.GlobalExceptionHandler;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.service.SolicitacaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoControllerTest {

    private static final String EMAIL_PROFESSOR = "adriano@ifsc.edu.br";

    private MockMvc mockMvc;

    @Mock
    private SolicitacaoService solicitacaoService;

    @InjectMocks
    private SolicitacaoController solicitacaoController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(solicitacaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor tokenProfessor() {
        return request -> {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("email", EMAIL_PROFESSOR)
                    .build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
            return request;
        };
    }

    @Test
    @DisplayName("POST /solicitacoes - Deve retornar 201 Created e o header Location com dados válidos")
    void criarSolicitacao_DeveRetornar201_QuandoDadosValidos() throws Exception {
        SolicitacaoResponseDTO responseMock = criarResponseDTOMock(1L, StatusSolicitacao.PENDENTE);

        when(solicitacaoService.criarSolicitacao(any(SolicitacaoRequestDTO.class))).thenReturn(responseMock);

        String requestBody = """
                {
                  "orientadorId": 1,
                  "aluno": {
                    "nome": "João Silva",
                    "email": "joao@ifsc.edu.br",
                    "curso": "Engenharia de Software"
                  },
                  "tema": "Inteligência Artificial na Educação",
                  "mensagem": "Gostaria de ser orientado por você."
                }
                """;

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/solicitacoes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.aluno.nome").value("João Silva"));
    }

    @Test
    @DisplayName("POST /solicitacoes - Deve retornar 400 Bad Request quando payload for inválido")
    void criarSolicitacao_DeveRetornar400_QuandoPayloadInvalido() throws Exception {
        String requestBody = """
                {
                  "orientadorId": null,
                  "aluno": {
                    "nome": "",
                    "email": "email-invalido",
                    "curso": ""
                  },
                  "tema": "IA",
                  "mensagem": ""
                }
                """;

        mockMvc.perform(post("/api/v1/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Erro de validação nos dados enviados"))
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    @Test
    @DisplayName("GET /solicitacoes/orientador/{id} - Deve retornar 200 OK com lista de solicitações")
    void listarPorOrientador_DeveRetornar200_ComLista() throws Exception {
        SolicitacaoResponseDTO response1 = criarResponseDTOMock(1L, StatusSolicitacao.PENDENTE);
        SolicitacaoResponseDTO response2 = criarResponseDTOMock(2L, StatusSolicitacao.ACEITA);

        when(solicitacaoService.listarPorOrientador(1L, null)).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/solicitacoes/orientador/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /solicitacoes/{id} - Deve retornar 200 OK com detalhes da solicitação")
    void buscarPorId_DeveRetornar200_QuandoIdExiste() throws Exception {
        SolicitacaoResponseDTO responseMock = criarResponseDTOMock(1L, StatusSolicitacao.RECUSADA);

        when(solicitacaoService.buscarPorId(1L)).thenReturn(responseMock);

        mockMvc.perform(get("/api/v1/solicitacoes/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RECUSADA"));
    }

    @Test
    @DisplayName("GET /solicitacoes/{id} - Deve retornar 404 Not Found quando solicitação não existe")
    void buscarPorId_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        when(solicitacaoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Solicitação com identificador 99 não foi encontrada."));

        mockMvc.perform(get("/api/v1/solicitacoes/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detalhes").value("Solicitação com identificador 99 não foi encontrada."));
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Deve retornar 200 OK ao atualizar status")
    void atualizarStatus_DeveRetornar200_QuandoPayloadValido() throws Exception {
        SolicitacaoResponseDTO responseMock = criarResponseDTOMock(1L, StatusSolicitacao.ACEITA);

        when(solicitacaoService.atualizarStatus(eq(1L), any(AtualizaStatusRequest.class), eq(EMAIL_PROFESSOR)))
                .thenReturn(responseMock);

        String requestBody = """
                {
                  "status": "ACEITA"
                }
                """;

        mockMvc.perform(patch("/api/v1/solicitacoes/1/status")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACEITA"));
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Deve retornar 403 Forbidden quando o professor não é o destinatário")
    void atualizarStatus_DeveRetornar403_QuandoProfessorNaoEhDestinatario() throws Exception {
        when(solicitacaoService.atualizarStatus(eq(1L), any(AtualizaStatusRequest.class), eq(EMAIL_PROFESSOR)))
                .thenThrow(new AcessoNegadoException(
                        "Somente o orientador destinatário pode aceitar ou recusar esta solicitação."));

        mockMvc.perform(patch("/api/v1/solicitacoes/1/status")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Somente o orientador destinatário pode aceitar ou recusar esta solicitação."));
    }

    private SolicitacaoResponseDTO criarResponseDTOMock(Long id, StatusSolicitacao status) {
        return new SolicitacaoResponseDTO(
                id,
                status,
                "Inteligência Artificial na Educação",
                "Gostaria de ser orientado por você.",
                status == StatusSolicitacao.RECUSADA ? "Não tenho vagas no momento." : null,
                Instant.now(),
                new SolicitacaoResponseDTO.AlunoResponse(10L, "João Silva", "joao@ifsc.edu.br", "Engenharia"),
                new SolicitacaoResponseDTO.OrientadorResponse(1L, "Dr. Adriano Lima", "adriano@ifsc.edu.br", 2)
        );
    }
}
