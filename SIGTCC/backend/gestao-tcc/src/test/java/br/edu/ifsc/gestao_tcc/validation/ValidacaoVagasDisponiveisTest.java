package br.edu.ifsc.gestao_tcc.validation;

import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.exception.VagasIndisponiveisException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacaoVagasDisponiveisTest {

    private ValidacaoVagasDisponiveis validacao;
    private SolicitacaoRequestDTO dtoMock;
    private Aluno alunoMock;

    @BeforeEach
    void setUp() {
        validacao = new ValidacaoVagasDisponiveis();
        dtoMock = new SolicitacaoRequestDTO();
        alunoMock = new Aluno();
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o orientador possui vagas disponíveis")
    void validar_ComVagasDisponiveis_NaoDeveLancarExcecao() {
        Orientador orientador = new Orientador();
        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(1);
        orientador.setPerfil(perfil);

        assertDoesNotThrow(() -> validacao.validar(dtoMock, alunoMock, orientador));
    }

    @Test
    @DisplayName("Deve lançar VagasIndisponiveisException quando vagas forem zero")
    void validar_SemVagasDisponiveis_DeveLancarExcecao() {
        Orientador orientador = new Orientador();
        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(0);
        orientador.setPerfil(perfil);

        VagasIndisponiveisException exception = assertThrows(
                VagasIndisponiveisException.class,
                () -> validacao.validar(dtoMock, alunoMock, orientador)
        );

        assertEquals("O orientador não possui vagas disponíveis.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar VagasIndisponiveisException quando vagas forem negativas")
    void validar_VagasNegativas_DeveLancarExcecao() {
        Orientador orientador = new Orientador();
        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(-1);
        orientador.setPerfil(perfil);

        VagasIndisponiveisException exception = assertThrows(
                VagasIndisponiveisException.class,
                () -> validacao.validar(dtoMock, alunoMock, orientador)
        );

        assertEquals("O orientador não possui vagas disponíveis.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar VagasIndisponiveisException quando o perfil for nulo")
    void validar_PerfilNulo_DeveLancarExcecao() {
        Orientador orientador = new Orientador();
        orientador.setPerfil(null);

        VagasIndisponiveisException exception = assertThrows(
                VagasIndisponiveisException.class,
                () -> validacao.validar(dtoMock, alunoMock, orientador)
        );

        assertEquals("O orientador não possui vagas disponíveis.", exception.getMessage());
    }
}