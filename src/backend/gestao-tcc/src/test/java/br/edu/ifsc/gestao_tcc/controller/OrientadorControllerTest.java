package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.GlobalExceptionHandler;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.service.OrientadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
        mockMvc = MockMvcBuilders.standaloneSetup(orientadorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listaOrientadorID_DeveRetornar200OK_QuandoIdExiste() throws Exception {
        OrientadorResponseDTO dto = new OrientadorResponseDTO(
                1L,
                "Dr. Adriano Lima",
                "adriano.lima@ifsc.edu.br",
                "DAE - Câmpus São José",
                List.of("Engenharia de Software"),
                3,
                "Professor com foco em processos de software.",
                true
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
    void listaOrientadorID_DeveRetornar404NotFound_QuandoIdNaoExiste() throws Exception {
        when(orientadorService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Orientador com identificador 99 não foi encontrado."));

        mockMvc.perform(get("/api/v1/orientadores/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/api/v1/orientadores/99"))
                .andExpect(jsonPath("$.detalhes").value("Orientador com identificador 99 não foi encontrado."));
    }

    @Test
    void atualizaOrientadorID_DeveRetornar200OK_QuandoDadosValidos() throws Exception {
        OrientadorResponseDTO responseDTO = new OrientadorResponseDTO(
                1L,
                "Dr. Adriano Lima",
                "adriano.lima@ifsc.edu.br",
                "DAE - Câmpus São José",
                List.of("Engenharia de Software", "IA"),
                5,
                "Nova biografia",
                true
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
                .andExpect(jsonPath("$.erro").value("Erro de validação nos dados enviados"));
    }

    @Test
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
}
