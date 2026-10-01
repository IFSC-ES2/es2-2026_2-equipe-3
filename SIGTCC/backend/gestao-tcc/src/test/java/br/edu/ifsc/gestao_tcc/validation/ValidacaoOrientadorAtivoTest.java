package br.edu.ifsc.gestao_tcc.validation;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.OrientadorInativoException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacaoOrientadorAtivoTest {

    private ValidacaoOrientadorAtivo validacao;
    private SolicitacaoRequestDTO dtoMock;
    private Aluno alunoMock;

    @BeforeEach
    void setUp() {
        validacao = new ValidacaoOrientadorAtivo();
        dtoMock = new SolicitacaoRequestDTO();
        alunoMock = new Aluno();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o orientador está ativo")
    void validar_OrientadorAtivo_NaoDeveLancarExcecao() {
        Orientador orientador = new Orientador();
        orientador.setAtivo(true);

        assertDoesNotThrow(() -> validacao.validar(dtoMock, alunoMock, orientador));
    }

    @Test
    @DisplayName("Deve lançar OrientadorInativoException quando o orientador está inativo")
    void validar_OrientadorInativo_DeveLancarExcecao() {
        Orientador orientador = new Orientador();
        orientador.setAtivo(false);

        OrientadorInativoException exception = assertThrows(
                OrientadorInativoException.class,
                () -> validacao.validar(dtoMock, alunoMock, orientador)
        );

        assertEquals("O orientador não está disponível para receber solicitações.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar OrientadorInativoException quando a flag ativo do orientador for nula")
    void validar_OrientadorComAtivoNulo_DeveLancarExcecao() {
        Orientador orientador = new Orientador();
        orientador.setAtivo(null);

        OrientadorInativoException exception = assertThrows(
                OrientadorInativoException.class,
                () -> validacao.validar(dtoMock, alunoMock, orientador)
        );

        assertEquals("O orientador não está disponível para receber solicitações.", exception.getMessage());
    }
}