package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.OrientadorResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.LinhaPesquisa;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void buscarPorId_DeveRetornarOrientadorResponseDTO_QuandoIdExiste() {
        Long id = 1L;

        PerfilOrientador perfil = PerfilOrientador.builder()
                .id(10L)
                .vagasDisponiveis(3)
                .biografia("Professor com foco em IA")
                .build();

        LinhaPesquisa linha = LinhaPesquisa.builder()
                .id(100L)
                .nome("Inteligência Artificial")
                .perfilOrientador(perfil)
                .build();

        perfil.setLinhasPesquisa(List.of(linha));

        Orientador orientador = Orientador.builder()
                .id(id)
                .nome("Dr. Adriano Lima")
                .email("adriano.lima@ifsc.edu.br")
                .departamento("DAE - Câmpus São José")
                .ativo(true)
                .perfil(perfil)
                .build();

        when(orientadorRepository.findById(id)).thenReturn(Optional.of(orientador));

        OrientadorResponseDTO dto = orientadorService.buscarPorId(id);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("Dr. Adriano Lima", dto.nome());
        assertEquals("adriano.lima@ifsc.edu.br", dto.email());
        assertEquals("DAE - Câmpus São José", dto.departamento());
        assertEquals(3, dto.vagasDisponiveis());
        assertEquals("Professor com foco em IA", dto.biografia());
        assertTrue(dto.ativo());
        assertEquals(1, dto.linhasDePesquisa().size());
        assertEquals("Inteligência Artificial", dto.linhasDePesquisa().get(0));

        verify(orientadorRepository, times(1)).findById(id);
    }

    @Test
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
}
