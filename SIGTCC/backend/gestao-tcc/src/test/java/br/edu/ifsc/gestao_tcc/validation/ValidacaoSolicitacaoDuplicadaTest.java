package br.edu.ifsc.gestao_tcc.validation;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.SolicitacaoDuplicadaException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidacaoSolicitacaoDuplicadaTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @InjectMocks
    private ValidacaoSolicitacaoDuplicada validacao;

    private SolicitacaoRequestDTO dtoMock;
    private Aluno alunoMock;
    private Orientador orientadorMock;

    @BeforeEach
    void setUp() {
        dtoMock = new SolicitacaoRequestDTO();

        alunoMock = new Aluno();
        alunoMock.setId(10L);

        orientadorMock = new Orientador();
        orientadorMock.setId(5L);
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o aluno não possui solicitação pendente para o orientador")
    void validar_SemSolicitacaoPendente_NaoDeveLancarExcecao() {
        when(solicitacaoRepository.existsByAlunoIdAndOrientadorIdAndStatus(10L, 5L, StatusSolicitacao.PENDENTE))
                .thenReturn(false);

        assertDoesNotThrow(() -> validacao.validar(dtoMock, alunoMock, orientadorMock));

        verify(solicitacaoRepository, times(1))
                .existsByAlunoIdAndOrientadorIdAndStatus(10L, 5L, StatusSolicitacao.PENDENTE);
    }

    @Test
    @DisplayName("Deve lançar SolicitacaoDuplicadaException quando já existe solicitação pendente")
    void validar_ComSolicitacaoPendente_DeveLancarExcecao() {
        when(solicitacaoRepository.existsByAlunoIdAndOrientadorIdAndStatus(10L, 5L, StatusSolicitacao.PENDENTE))
                .thenReturn(true);

        SolicitacaoDuplicadaException exception = assertThrows(
                SolicitacaoDuplicadaException.class,
                () -> validacao.validar(dtoMock, alunoMock, orientadorMock)
        );

        assertEquals("Você possui uma solicitação pendente para este orientador.", exception.getMessage());

        verify(solicitacaoRepository, times(1))
                .existsByAlunoIdAndOrientadorIdAndStatus(10L, 5L, StatusSolicitacao.PENDENTE);
    }
}