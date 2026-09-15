package br.edu.ifsc.gestao_tcc.repository;

import br.edu.ifsc.gestao_tcc.model.LinhaPesquisa;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class OrientadorRepositoryTest {

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Orientador salvarOrientadorComPerfil(String nome, String email, String nomeLinhaPesquisa) {
        Orientador orientador = new Orientador();
        orientador.setNome(nome);
        orientador.setEmail(email);
        orientador.setDepartamento("Engenharia");
        orientador.setAtivo(true);

        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setBiografia("Biografia de teste");
        perfil.setVagasDisponiveis(3);
        perfil.setOrientador(orientador);

        LinhaPesquisa linha = new LinhaPesquisa();
        linha.setNome(nomeLinhaPesquisa);
        linha.setPerfilOrientador(perfil);

        List<LinhaPesquisa> linhas = new ArrayList<>();
        linhas.add(linha);
        perfil.setLinhasPesquisa(linhas);

        orientador.setPerfil(perfil);

        return entityManager.persistAndFlush(orientador);
    }

    @Test
    @DisplayName("Deve retornar true quando o email já existir no banco")
    void existsByEmail_QuandoEmailExiste_DeveRetornarTrue() {
        String email = "teste@ifsc.edu.br";
        salvarOrientadorComPerfil("João", email, "Segurança");

        boolean existe = orientadorRepository.existsByEmail(email);

        assertTrue(existe);
    }

    @Test
    @DisplayName("Deve retornar false quando o email não existir no banco")
    void existsByEmail_QuandoEmailNaoExiste_DeveRetornarFalse() {
        String emailExistente = "teste@ifsc.edu.br";
        String emailBuscado = "novo@ifsc.edu.br";
        salvarOrientadorComPerfil("João", emailExistente, "Segurança");

        boolean existe = orientadorRepository.existsByEmail(emailBuscado);

        assertFalse(existe);
    }

    @Test
    @DisplayName("Deve retornar uma lista de orientadores filtrando por um trecho da linha de pesquisa")
    void findByLinhasPesquisaNomeContainingIgnoreCase_ComMatch_DeveRetornarLista() {
        salvarOrientadorComPerfil("Ana", "ana@ifsc.edu.br", "Inteligência Artificial");
        salvarOrientadorComPerfil("Carlos", "carlos@ifsc.edu.br", "Desenvolvimento Web");

        List<Orientador> resultado = orientadorRepository
                .findByLinhasPesquisaNomeContainingIgnoreCase("inteligência");

        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando nenhuma linha de pesquisa corresponder à busca")
    void findByLinhasPesquisaNomeContainingIgnoreCase_SemMatch_DeveRetornarListaVazia() {
        salvarOrientadorComPerfil("Ana", "ana@ifsc.edu.br", "Redes de Computadores");

        List<Orientador> resultado = orientadorRepository
                .findByLinhasPesquisaNomeContainingIgnoreCase("Robótica"); //[cite: 14]

        assertThat(resultado).isEmpty();
    }
}