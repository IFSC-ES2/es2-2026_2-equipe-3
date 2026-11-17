package br.edu.ifsc.gestao_tcc.controller;


import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.AcessoNegadoException;
import br.edu.ifsc.gestao_tcc.exception.EmailDuplicadoException;
import br.edu.ifsc.gestao_tcc.exception.GlobalExceptionHandler;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.service.OrientadorService;
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

import java.time.Clock;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ExtendWith(MockitoExtension.class)
class OrientadorControllerTest {

    private static final String EMAIL_PROFESSOR = "adriano.lima@ifsc.edu.br";

    private MockMvc mockMvc;

    @Mock
    private OrientadorService orientadorService;

    @InjectMocks
    private OrientadorController orientadorController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(orientadorController)
                .setControllerAdvice(new GlobalExceptionHandler(Clock.systemUTC()))
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    // Setup standalone não tem o filtro do Spring Security: o contexto é preenchido
    // diretamente para que o @AuthenticationPrincipal resolva o Jwt.
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
    @DisplayName("POST /orientadores - Deve retornar 201 Created quando dados são válidos")
    void cadastraOrientador_DeveRetornar201_QuandoDadosValidos() throws Exception {
        Orientador orientadorMock = new Orientador();
        orientadorMock.setId(1L);

        when(this.orientadorService.cadastrar(any(OrientadorRequestDTO.class))).thenReturn(orientadorMock);

        String RequestBody = """
                {
                    "nome": "João Silva",
                    "email": "joao.silva@ifsc.edu.br",
                    "linhasDePesquisa": ["IA"],
                    "vagasDisponiveis": 3
                }
                """;

        mockMvc.perform(post("/api/v1/orientadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/orientadores/1"));
    }

    @Test
    @DisplayName("POST /orientadores - Deve retornar 400 Bad Request quando dados são inválidos")
    void cadastraOrientador_DeveRetornar400_QuandoDadosInvalidos() throws Exception {
        String RequestBody = """
                {
                  "nome": "",
                  "email": "email-invalido",
                  "vagasDisponiveis": -1
                }
                """;

        mockMvc.perform(post("/api/v1/orientadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Erro de validação nos dados enviados"))
                .andExpect(jsonPath("$.detalhes").isArray())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'nome')]").exists())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'email')]").exists())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'linhasDePesquisa')]").exists())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'vagasDisponiveis')]").exists());
    }

    @Test
    @DisplayName("POST /orientadores - Deve retornar 409 Conflict quando email já existe")
    void cadastraOrientador_DeveRetornar409_QuandoEmailDuplicado() throws Exception {
        when(orientadorService.cadastrar(any(OrientadorRequestDTO.class)))
                .thenThrow(new EmailDuplicadoException("Já existe um orientador cadastrado com o e-mail informado."));

        String RequestBody = """
                {
                  "nome": "João Silva",
                  "email": "joao.silva@ifsc.edu.br",
                  "linhasDePesquisa": ["IA"],
                  "vagasDisponiveis": 3
                }
                """;

        mockMvc.perform(post("/api/v1/orientadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RequestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflito de dados"));
    }

    @Test
    @DisplayName("GET /orientadores - Deve retornar 200 OK com lista de orientadores (sem filtro)")
    void listaOrientadores_DeveRetornar200OK_ComListaCompleta() throws Exception {
        OrientadorResponseDTO dto1 = new OrientadorResponseDTO(1L, "Dr. Adriano Lima", "adriano.lima@ifsc.edu.br", "DAE", List.of("Engenharia de Software"), 3, "Bio", true);
        OrientadorResponseDTO dto2 = new OrientadorResponseDTO(2L, "Dra. Maria", "maria@ifsc.edu.br", "DAE", List.of("IA"), 2, "Bio", true);

        when(orientadorService.listarOrientadores(null, null)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/orientadores")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Dr. Adriano Lima"))
                .andExpect(jsonPath("$[1].nome").value("Dra. Maria"));
    }

    @Test
    @DisplayName("GET /orientadores?area=IA - Deve retornar 200 OK com lista filtrada")
    void listaOrientadores_DeveRetornar200OK_ComFiltroDeArea() throws Exception {
        OrientadorResponseDTO dto2 = new OrientadorResponseDTO(2L, "Dra. Maria", "maria@ifsc.edu.br", "DAE", List.of("IA"), 2, "Bio", true);

        when(orientadorService.listarOrientadores("IA", null)).thenReturn(List.of(dto2));

        mockMvc.perform(get("/api/v1/orientadores")
                        .param("area", "IA")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].linhasDePesquisa[0]").value("IA"));
    }

    @Test
    @DisplayName("GET /orientadores?temVagas=true - Deve retornar 200 OK com lista filtrada por vagas")
    void listaOrientadores_DeveRetornar200OK_ComFiltroDeVagas() throws Exception {
        OrientadorResponseDTO dto = new OrientadorResponseDTO(
                1L,
                "Dr. Adriano Lima",
                "adriano.lima@ifsc.edu.br",
                "DAE",
                List.of("Engenharia de Software"),
                3,
                "Bio",
                true
        );

        when(orientadorService.listarOrientadores(null, true))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orientadores")
                        .param("temVagas", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Dr. Adriano Lima"))
                .andExpect(jsonPath("$[0].vagasDisponiveis").value(3));
    }

    @Test
    @DisplayName("GET /orientadores?area=IA&temVagas=true - Deve retornar 200 OK com filtros combinados")
    void listaOrientadores_DeveRetornar200OK_ComFiltrosCombinados() throws Exception {
        OrientadorResponseDTO dto = new OrientadorResponseDTO(
                1L,
                "Dra. Maria",
                "maria@ifsc.edu.br",
                "DAE",
                List.of("IA"),
                2,
                "Bio",
                true
        );

        when(orientadorService.listarOrientadores("IA", true))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/orientadores")
                        .param("area", "IA")
                        .param("temVagas", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].linhasDePesquisa[0]").value("IA"))
                .andExpect(jsonPath("$[0].vagasDisponiveis").value(2));
    }

    @Test
    @DisplayName("GET /orientadores/{id} - Deve retornar 200 OK quando Id existe")
    void listaOrientadorID_DeveRetornar200OK_QuandoIdExiste() throws Exception {
        OrientadorResponseDTO dto = new OrientadorResponseDTO(
                1L, "Dr. Adriano Lima", "adriano.lima@ifsc.edu.br", "DAE - Câmpus São José",
                List.of("Engenharia de Software"), 3, "Professor com foco em processos de software.", true
        );

        when(orientadorService.buscarPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/orientadores/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Dr. Adriano Lima"))
                .andExpect(jsonPath("$.email").value("adriano.lima@ifsc.edu.br"))
                .andExpect(jsonPath("$.vagasDisponiveis").value(3))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("GET /orientadores/{id} - Deve retornar 404 Not Found quando Id não existe")
    void listaOrientadorID_DeveRetornar404NotFound_QuandoIdNaoExiste() throws Exception {

        when(orientadorService.buscarPorId(1L))
                .thenThrow(new ResourceNotFoundException("Orientador com identificador 1 não foi encontrado."));

        mockMvc.perform(get("/api/v1/orientadores/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/api/v1/orientadores/1"))
                .andExpect(jsonPath("$.detalhes").value("Orientador com identificador 1 não foi encontrado."));
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Deve retornar 200 OK quando dados de atualização são válidos")
    void atualizaOrientadorID_DeveRetornar200OK_QuandoDadosValidos() throws Exception {
        OrientadorResponseDTO responseDTO = new OrientadorResponseDTO(
                1L, "Dr. Adriano Lima", "adriano.lima@ifsc.edu.br", "DAE - Câmpus São José",
                List.of("Engenharia de Software", "IA"), 5, "Nova biografia", true
        );

        when(orientadorService.atualizar(eq(1L), any(), eq(EMAIL_PROFESSOR))).thenReturn(responseDTO);

        String jsonRequestBody = """
                {
                    "vagasDisponiveis": 5,
                    "biografia": "Nova biografia"
                }
                """;

        mockMvc.perform(patch("/api/v1/orientadores/1")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.vagasDisponiveis").value(5))
                .andExpect(jsonPath("$.biografia").value("Nova biografia"));
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Deve retornar 400 Bad Request quando dados de atualização violam validações")
    void atualizaOrientadorID_DeveRetornar400BadRequest_QuandoVagasNegativas() throws Exception {
        String jsonRequestBody = """
                {
                    "vagasDisponiveis": -1
                }
                """;

        mockMvc.perform(patch("/api/v1/orientadores/1")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Erro de validação nos dados enviados"))
                .andExpect(jsonPath("$.detalhes").isArray())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'vagasDisponiveis')]").exists());
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Deve retornar 404 Not Found quando Id a ser atualizado não existe")
    void atualizaOrientadorID_DeveRetornar404NotFound_QuandoIdNaoExiste() throws Exception {
        when(orientadorService.atualizar(eq(99L), any(), eq(EMAIL_PROFESSOR)))
                .thenThrow(new ResourceNotFoundException("Orientador com identificador 99 não foi encontrado."));

        String jsonRequestBody = """
                {
                    "vagasDisponiveis": 5
                }
                """;

        mockMvc.perform(patch("/api/v1/orientadores/99")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("DELETE /orientadores/{id} - Deve retornar 204 No Content quando Id existe e é desativado")
    void deletaOrientadorID_DeveRetornar204_QuandoIdExiste() throws Exception {
        doNothing().when(orientadorService).deletar(1L, EMAIL_PROFESSOR);

        mockMvc.perform(delete("/api/v1/orientadores/1").with(tokenProfessor()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(orientadorService).deletar(1L, EMAIL_PROFESSOR);
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Deve retornar 403 Forbidden quando o professor não é dono do cadastro")
    void atualizaOrientadorID_DeveRetornar403_QuandoNaoEhDono() throws Exception {
        when(orientadorService.atualizar(eq(2L), any(), eq(EMAIL_PROFESSOR)))
                .thenThrow(new AcessoNegadoException("Você só pode alterar o seu próprio cadastro de orientador."));

        mockMvc.perform(patch("/api/v1/orientadores/2")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vagasDisponiveis\": 0}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    @DisplayName("DELETE /orientadores/{id} - Deve retornar 404 Not Found quando Id a ser deletado não existe")
    void deletaOrientadorID_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        doThrow(new ResourceNotFoundException("Orientador com identificador 99 não foi encontrado."))
                .when(orientadorService).deletar(99L, EMAIL_PROFESSOR);

        mockMvc.perform(delete("/api/v1/orientadores/99").with(tokenProfessor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"));
    }

    // --- Limites de tamanho dos DTOs (issue #273) ---

    private static final String URL = "/api/v1/orientadores";

    private String payloadCadastro(String nome, String email, String departamento, String biografia, String linha) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "departamento": "%s",
                  "biografia": "%s",
                  "linhasDePesquisa": ["%s"],
                  "vagasDisponiveis": 3
                }
                """.formatted(nome, email, departamento, biografia, linha);
    }

    /** E-mail em formato válido (parte local ≤ 64 e rótulos de domínio ≤ 63) com o tamanho total pedido. */
    private String emailComTamanho(int tamanho) {
        String local = "a".repeat(40);
        String sufixo = ".ifsc.edu.br";
        return local + "@" + "b".repeat(tamanho - local.length() - 1 - sufixo.length()) + sufixo;
    }

    @Test
    @DisplayName("POST /orientadores - nome com mais de 100 caracteres deve retornar 400")
    void cadastraOrientador_DeveRetornar400_QuandoNomeExcedeLimite() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro("a".repeat(101), "joao@ifsc.edu.br", "DAE", "Bio", "IA")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'nome')].mensagem")
                        .value("O nome deve ter entre 3 e 100 caracteres."));

        verify(orientadorService, never()).cadastrar(any());
    }

    @Test
    @DisplayName("POST /orientadores - departamento acima de 100 e biografia acima de 500 caracteres devem retornar 400")
    void cadastraOrientador_DeveRetornar400_QuandoDepartamentoEBiografiaExcedemLimite() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro("João Silva", "joao@ifsc.edu.br",
                                "d".repeat(101), "b".repeat(501), "IA")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'departamento')].mensagem")
                        .value("O departamento deve ter no máximo 100 caracteres."))
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'biografia')].mensagem")
                        .value("A biografia deve ter no máximo 500 caracteres."));
    }

    @Test
    @DisplayName("POST /orientadores - e-mail válido com mais de 100 caracteres deve retornar 400")
    void cadastraOrientador_DeveRetornar400_QuandoEmailExcedeLimite() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro("João Silva", emailComTamanho(101), "DAE", "Bio", "IA")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'email')].mensagem")
                        .value("O e-mail deve ter no máximo 100 caracteres."));
    }

    @Test
    @DisplayName("POST /orientadores - linha de pesquisa com mais de 80 caracteres deve retornar 400 indicando o item")
    void cadastraOrientador_DeveRetornar400_QuandoLinhaDePesquisaExcedeLimite() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro("João Silva", "joao@ifsc.edu.br", "DAE", "Bio", "l".repeat(81))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'linhasDePesquisa[0]')].mensagem")
                        .value("A linha de pesquisa deve ter entre 2 e 80 caracteres."));
    }

    @Test
    @DisplayName("POST /orientadores - linha de pesquisa em branco deve retornar 400 indicando o item")
    void cadastraOrientador_DeveRetornar400_QuandoLinhaDePesquisaEmBranco() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro("João Silva", "joao@ifsc.edu.br", "DAE", "Bio", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'linhasDePesquisa[0]' "
                        + "&& @.mensagem == 'A linha de pesquisa não pode ficar em branco.')]").exists());
    }

    @Test
    @DisplayName("POST /orientadores - valores exatamente no limite devem ser aceitos (201)")
    void cadastraOrientador_DeveRetornar201_QuandoValoresNoLimiteExato() throws Exception {
        Orientador orientadorMock = new Orientador();
        orientadorMock.setId(1L);
        when(orientadorService.cadastrar(any(OrientadorRequestDTO.class))).thenReturn(orientadorMock);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadCadastro("a".repeat(100), emailComTamanho(100),
                                "d".repeat(100), "b".repeat(500), "l".repeat(80))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - lista vazia de linhas de pesquisa deve retornar 400")
    void atualizaOrientadorID_DeveRetornar400_QuandoListaDeLinhasVazia() throws Exception {
        mockMvc.perform(patch(URL + "/1")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"linhasDePesquisa\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'linhasDePesquisa')].mensagem")
                        .value("Informe ao menos uma linha de pesquisa."));

        verify(orientadorService, never()).atualizar(any(), any(), any());
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - linha de pesquisa com mais de 80 caracteres deve retornar 400")
    void atualizaOrientadorID_DeveRetornar400_QuandoLinhaDePesquisaExcedeLimite() throws Exception {
        mockMvc.perform(patch(URL + "/1")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"linhasDePesquisa\": [\"" + "l".repeat(81) + "\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[?(@.campo == 'linhasDePesquisa[0]')]").exists());
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - sem linhasDePesquisa continua sendo atualização parcial válida (200)")
    void atualizaOrientadorID_DeveRetornar200_QuandoLinhasDePesquisaOmitidas() throws Exception {
        when(orientadorService.atualizar(eq(1L), any(), eq(EMAIL_PROFESSOR)))
                .thenReturn(new OrientadorResponseDTO(1L, "Dr. Adriano Lima", EMAIL_PROFESSOR, "DAE",
                        List.of("Engenharia de Software"), 4, "Bio", true));

        mockMvc.perform(patch(URL + "/1")
                        .with(tokenProfessor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vagasDisponiveis\": 4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vagasDisponiveis").value(4));
    }
}
