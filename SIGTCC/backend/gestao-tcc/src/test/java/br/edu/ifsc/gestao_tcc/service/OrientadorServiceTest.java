package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.OrientadorRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.OrientadorUpdateDTO;
import br.edu.ifsc.gestao_tcc.exception.EmailDuplicadoException;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.LinhaPesquisa;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrientadorServiceTest {
    @Mock
    private OrientadorRepository orientadorRepository;

    @InjectMocks
    private OrientadorService orientadorService;

    @Test
    @DisplayName("Deve cadastrar um orientador com sucesso")
    void cadastrar_DeveSalvarOrientador_QuandoDadosValidos() {
        OrientadorRequestDTO requestDTO = new OrientadorRequestDTO();
        requestDTO.setNome("João Silva");
        requestDTO.setEmail("joao@ifsc.edu.br");
        requestDTO.setDepartamento("DAE");
        requestDTO.setVagasDisponiveis(3);
        requestDTO.setBiografia("Professor Doutor");
        requestDTO.setLinhasDePesquisa(List.of("IA", "Redes"));

        when(orientadorRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(orientadorRepository.save(any(Orientador.class))).thenAnswer(invocation -> {
            Orientador o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        Orientador salvo = orientadorService.cadastrar(requestDTO);

        assertNotNull(salvo);
        assertEquals(1L, salvo.getId());
        assertEquals("João Silva", salvo.getNome());
        assertNotNull(salvo.getPerfil());
        assertEquals(3, salvo.getPerfil().getVagasDisponiveis());
        assertEquals(2, salvo.getPerfil().getLinhasPesquisa().size());

        verify(orientadorRepository, times(1)).existsByEmail(anyString());
        verify(orientadorRepository, times(1)).save(any(Orientador.class));
    }

    @Test
    @DisplayName("Deve lançar EmailDuplicadoException quando e-mail já existir no cadastro")
    void cadastrar_DeveLancarEmailDuplicadoException_QuandoEmailJaExiste() {
        OrientadorRequestDTO requestDTO = new OrientadorRequestDTO();
        requestDTO.setEmail("joao@ifsc.edu.br");

        when(orientadorRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        assertThrows(EmailDuplicadoException.class, () -> orientadorService.cadastrar(requestDTO));

        verify(orientadorRepository, times(1)).existsByEmail(anyString());
        verify(orientadorRepository, never()).save(any(Orientador.class));
    }

    @Test
    @DisplayName("Deve retornar todos os orientadores quando não passar filtro de área")
    void listarOrientadores_DeveRetornarTodos_QuandoFiltroForNuloOuVazio() {
        Orientador o1 = criarOrientadorMock(1L, "João");
        Orientador o2 = criarOrientadorMock(2L, "Maria");

        when(orientadorRepository.findAll()).thenReturn(List.of(o1, o2));

        List<OrientadorResponseDTO> resultado = orientadorService.listarOrientadores(null);

        assertEquals(2, resultado.size());
        verify(orientadorRepository, times(1)).findAll();
        verify(orientadorRepository, never()).findByLinhasPesquisaNomeContainingIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Deve retornar orientadores filtrados pela área")
    void listarOrientadores_DeveRetornarFiltrados_QuandoPassarFiltro() {
        Orientador o1 = criarOrientadorMock(1L, "João");

        when(orientadorRepository.findByLinhasPesquisaNomeContainingIgnoreCase("IA"))
                .thenReturn(List.of(o1));

        List<OrientadorResponseDTO> resultado = orientadorService.listarOrientadores("IA");

        assertEquals(1, resultado.size());
        assertEquals("João", resultado.get(0).nome());
        verify(orientadorRepository, times(1)).findByLinhasPesquisaNomeContainingIgnoreCase("IA");
        verify(orientadorRepository, never()).findAll();
    }

    @Test
    @DisplayName("Deve retornar OrientadorResponseDTO quando ID existir")
    void buscarPorId_DeveRetornarOrientadorResponseDTO_QuandoIdExiste() {
        Long id = 1L;
        Orientador orientador = criarOrientadorMock(id, "Dr. Adriano Lima");
        orientador.setEmail("adriano.lima@ifsc.edu.br");
        orientador.setDepartamento("DAE - Câmpus São José");

        when(orientadorRepository.findById(id)).thenReturn(Optional.of(orientador));

        OrientadorResponseDTO dto = orientadorService.buscarPorId(id);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("Dr. Adriano Lima", dto.nome());
        assertEquals("adriano.lima@ifsc.edu.br", dto.email());
        assertEquals("DAE - Câmpus São José", dto.departamento());
        assertEquals(3, dto.vagasDisponiveis());
        assertTrue(dto.ativo());

        verify(orientadorRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    void buscarPorId_DeveLancarResourceNotFoundException_QuandoIdNaoExiste() {
        Long id = 99L;
        when(orientadorRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orientadorService.buscarPorId(id)
        );

        assertEquals("Orientador com identificador 99 não foi encontrado.", exception.getMessage());
        verify(orientadorRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar os campos passados e retornar o DTO")
    void atualizar_DeveAtualizarCamposEEnviarResponseDTO_QuandoDadosValidos() {
        Long id = 1L;
        Orientador orientador = criarOrientadorMock(id, "Dr. Adriano Lima");

        OrientadorUpdateDTO updateDTO = new OrientadorUpdateDTO(
                null, null, null, List.of("Engenharia de Software"), 5, "Nova biografia"
        );

        when(orientadorRepository.findById(id)).thenReturn(Optional.of(orientador));
        when(orientadorRepository.save(any(Orientador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrientadorResponseDTO response = orientadorService.atualizar(id, updateDTO);

        assertNotNull(response);
        assertEquals(5, response.vagasDisponiveis());
        assertEquals("Nova biografia", response.biografia());
        assertEquals(1, response.linhasDePesquisa().size());
        assertEquals("Engenharia de Software", response.linhasDePesquisa().get(0));

        verify(orientadorRepository, times(1)).findById(id);
        verify(orientadorRepository, times(1)).save(orientador);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar ID inexistente")
    void atualizar_DeveLancarResourceNotFoundException_QuandoIdNaoExiste() {
        Long id = 99L;
        OrientadorUpdateDTO updateDTO = new OrientadorUpdateDTO(null, null, null, null, 5, null);

        when(orientadorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orientadorService.atualizar(id, updateDTO));

        verify(orientadorRepository, times(1)).findById(id);
        verify(orientadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar o orientador logicamente quando o ID for encontrado")
    void deletar_DeveDesativarOrientador_QuandoIdExiste() {
        Long id = 1L;
        Orientador orientador = criarOrientadorMock(id, "João");
        orientador.setAtivo(true); // Garante que está ativo antes de deletar

        when(orientadorRepository.findById(id)).thenReturn(Optional.of(orientador));

        orientadorService.deletar(id);

        ArgumentCaptor<Orientador> captor = ArgumentCaptor.forClass(Orientador.class);
        verify(orientadorRepository, times(1)).save(captor.capture());

        Orientador orientadorSalvo = captor.getValue();
        assertFalse(orientadorSalvo.getAtivo()); // Valida a exclusão lógica
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar ID inexistente")
    void deletar_DeveLancarResourceNotFoundException_QuandoIdNaoExiste() {
        Long id = 99L;

        when(orientadorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orientadorService.deletar(id));

        verify(orientadorRepository, times(1)).findById(id);
        verify(orientadorRepository, never()).save(any());
    }

    private Orientador criarOrientadorMock(Long id, String nome) {
        PerfilOrientador perfil = PerfilOrientador.builder()
                .vagasDisponiveis(3)
                .biografia("Biografia mock")
                .build();

        LinhaPesquisa linha = LinhaPesquisa.builder()
                .nome("Inteligência Artificial")
                .perfilOrientador(perfil)
                .build();
        perfil.setLinhasPesquisa(new ArrayList<>(List.of(linha)));

        return Orientador.builder()
                .id(id)
                .nome(nome)
                .ativo(true)
                .perfil(perfil)
                .build();
    }
}
