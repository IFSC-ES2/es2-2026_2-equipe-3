package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.PerfilProfessorResponseDTO;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilProfessorServiceTest {

    @Mock
    private OrientadorRepository orientadorRepository;

    @Mock
    private TurmaTCCRepository turmaTCCRepository;

    @InjectMocks
    private PerfilProfessorService perfilProfessorService;

    private Orientador professor(Long id, String email) {
        Orientador professor = new Orientador();
        professor.setId(id);
        professor.setNome("Professor Teste");
        professor.setEmail(email);
        professor.setDepartamento("DAELN");
        return professor;
    }

    @Test
    @DisplayName("Deve retornar responsavelTurma true quando o professor é responsável pela turma vigente")
    void buscarPerfilPorEmail_QuandoResponsavel_DeveRetornarTrue() {
        when(orientadorRepository.findByEmailIgnoreCase("prof@ifsc.edu.br"))
                .thenReturn(Optional.of(professor(5L, "prof@ifsc.edu.br")));
        when(turmaTCCRepository.existsByProfessorResponsavelIdAndVigenteTrue(5L)).thenReturn(true);

        PerfilProfessorResponseDTO perfil = perfilProfessorService.buscarPerfilPorEmail("prof@ifsc.edu.br");

        assertThat(perfil.id()).isEqualTo(5L);
        assertThat(perfil.nome()).isEqualTo("Professor Teste");
        assertThat(perfil.email()).isEqualTo("prof@ifsc.edu.br");
        assertThat(perfil.departamento()).isEqualTo("DAELN");
        assertThat(perfil.responsavelTurma()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar responsavelTurma false quando o professor não é responsável pela turma vigente")
    void buscarPerfilPorEmail_QuandoNaoResponsavel_DeveRetornarFalse() {
        when(orientadorRepository.findByEmailIgnoreCase("prof@ifsc.edu.br"))
                .thenReturn(Optional.of(professor(5L, "prof@ifsc.edu.br")));
        when(turmaTCCRepository.existsByProfessorResponsavelIdAndVigenteTrue(5L)).thenReturn(false);

        PerfilProfessorResponseDTO perfil = perfilProfessorService.buscarPerfilPorEmail("prof@ifsc.edu.br");

        assertThat(perfil.responsavelTurma()).isFalse();
        verify(turmaTCCRepository).existsByProfessorResponsavelIdAndVigenteTrue(5L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando não existe professor com o e-mail informado")
    void buscarPerfilPorEmail_QuandoProfessorNaoExiste_DeveLancarExcecao() {
        when(orientadorRepository.findByEmailIgnoreCase("nao.existe@ifsc.edu.br")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> perfilProfessorService.buscarPerfilPorEmail("nao.existe@ifsc.edu.br"));

        verifyNoInteractions(turmaTCCRepository);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o e-mail é nulo ou em branco")
    void buscarPerfilPorEmail_QuandoEmailAusente_DeveLancarExcecao() {
        assertThrows(ResourceNotFoundException.class, () -> perfilProfessorService.buscarPerfilPorEmail(null));
        assertThrows(ResourceNotFoundException.class, () -> perfilProfessorService.buscarPerfilPorEmail("  "));

        verifyNoInteractions(orientadorRepository);
        verifyNoInteractions(turmaTCCRepository);
    }
}
