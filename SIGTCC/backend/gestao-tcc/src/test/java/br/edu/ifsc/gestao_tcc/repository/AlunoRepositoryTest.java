package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.Aluno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AlunoRepositoryTest {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve retornar um aluno quando pesquisar por um e-mail existente")
    void findByEmail_QuandoEmailExiste_DeveRetornarAluno() {
        Aluno aluno = new Aluno().builder()
                .nome("Maria Silva")
                .email("maria@ifsc.edu.br")
                .curso("Engenharia de Telecomunicações")
                .build();

        entityManager.persistAndFlush(aluno);

        Optional<Aluno> alunoEncontrado = alunoRepository.findByEmail("maria@ifsc.edu.br");

        assertTrue(alunoEncontrado.isPresent());
        assertThat(alunoEncontrado.get().getNome()).isEqualTo("Maria Silva");
        assertThat(alunoEncontrado.get().getCurso()).isEqualTo("Engenharia de Telecomunicações");
    }

    @Test
    @DisplayName("Deve retornar um Optional vazio quando pesquisar por um e-mail que não existe")
    void findByEmail_QuandoEmailNaoExiste_DeveRetornarVazio() {
        Optional<Aluno> alunoEncontrado = alunoRepository.findByEmail("nao_existe@ifsc.edu.br");

        assertFalse(alunoEncontrado.isPresent());
    }
}