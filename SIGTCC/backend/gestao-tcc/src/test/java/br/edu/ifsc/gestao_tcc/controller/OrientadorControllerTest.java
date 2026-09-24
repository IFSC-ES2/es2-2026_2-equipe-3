package br.edu.ifsc.gestao_tcc.controller;


import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.EmailDuplicadoException;
import br.edu.ifsc.gestao_tcc.exception.GlobalExceptionHandler;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.service.OrientadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ExtendWith(MockitoExtension.class)
class OrientadorControllerTest {

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
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
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

        when(orientadorService.listarOrientadores(null)).thenReturn(List.of(dto1, dto2));

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

        when(orientadorService.listarOrientadores("IA")).thenReturn(List.of(dto2));

        mockMvc.perform(get("/api/v1/orientadores")
                        .param("area", "IA")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].linhasDePesquisa[0]").value("IA"));
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

        when(orientadorService.atualizar(eq(1L), any())).thenReturn(responseDTO);

        String jsonRequestBody = """
                {
                    "vagasDisponiveis": 5,
                    "biografia": "Nova biografia"
                }
                """;

        mockMvc.perform(patch("/api/v1/orientadores/1")
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
        when(orientadorService.atualizar(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Orientador com identificador 99 não foi encontrado."));

        String jsonRequestBody = """
                {
                    "vagasDisponiveis": 5
                }
                """;

        mockMvc.perform(patch("/api/v1/orientadores/99")
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
        doNothing().when(orientadorService).deletar(1L);

        mockMvc.perform(delete("/api/v1/orientadores/1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @DisplayName("DELETE /orientadores/{id} - Deve retornar 404 Not Found quando Id a ser deletado não existe")
    void deletaOrientadorID_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        doThrow(new ResourceNotFoundException("Orientador com identificador 99 não foi encontrado."))
                .when(orientadorService).deletar(99L);

        mockMvc.perform(delete("/api/v1/orientadores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"));
    }
}
