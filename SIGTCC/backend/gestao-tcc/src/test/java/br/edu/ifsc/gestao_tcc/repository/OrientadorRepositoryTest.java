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
import java.util.Optional;

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
    @DisplayName("Deve encontrar o orientador pelo e-mail ignorando maiúsculas e minúsculas")
    void findByEmailIgnoreCase_QuandoEmailExisteComCaixaDiferente_DeveRetornarOrientador() {
        salvarOrientadorComPerfil("João", "Joao.Silva@ifsc.edu.br", "Segurança");

        Optional<Orientador> encontrado = orientadorRepository.findByEmailIgnoreCase("joao.silva@ifsc.edu.br");

        assertTrue(encontrado.isPresent());
        assertThat(encontrado.get().getNome()).isEqualTo("João");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o e-mail não pertence a nenhum orientador")
    void findByEmailIgnoreCase_QuandoEmailNaoExiste_DeveRetornarVazio() {
        salvarOrientadorComPerfil("João", "joao@ifsc.edu.br", "Segurança");

        Optional<Orientador> encontrado = orientadorRepository.findByEmailIgnoreCase("outro@ifsc.edu.br");

        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Deve retornar uma lista de orientadores filtrando por um trecho da linha de pesquisa")
    void findByFiltros_ComMatchNaLinhaDePesquisa_DeveRetornarLista() {
        salvarOrientadorComPerfil("Ana", "ana@ifsc.edu.br", "Inteligência Artificial");
        salvarOrientadorComPerfil("Carlos", "carlos@ifsc.edu.br", "Desenvolvimento Web");

        List<Orientador> resultado = orientadorRepository
                .findByFiltros("inteligência", null);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando nenhuma linha de pesquisa corresponder à busca")
    void findByFiltros_SemMatchNaLinhaDePesquisa_DeveRetornarListaVazia() {
        salvarOrientadorComPerfil("Ana", "ana@ifsc.edu.br", "Redes de Computadores");

        List<Orientador> resultado = orientadorRepository
                .findByFiltros("Robótica", null);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar somente orientadores com vagas disponíveis")
    void findByFiltros_ComFiltroDeVagas_DeveRetornarSomenteComVagas() {
        Orientador comVagas = salvarOrientadorComPerfil(
                "Ana",
                "ana@ifsc.edu.br",
                "Inteligência Artificial"
        );

        Orientador semVagas = salvarOrientadorComPerfil(
                "Carlos",
                "carlos@ifsc.edu.br",
                "Desenvolvimento Web"
        );

        comVagas.getPerfil().setVagasDisponiveis(2);
        semVagas.getPerfil().setVagasDisponiveis(0);

        entityManager.flush();
        entityManager.clear();

        List<Orientador> resultado =
                orientadorRepository.findByFiltros(null, true);

        assertThat(resultado)
                .extracting(Orientador::getNome)
                .containsExactly("Ana");
    }

    @Test
    @DisplayName("Deve incluir orientador com exatamente 1 vaga e excluir o com 0 (limite de vagasDisponiveis > 0)")
    void findByFiltros_ComFiltroDeVagas_DeveRespeitarLimiteExatoDeZeroEUmaVaga() {
        Orientador umaVaga = salvarOrientadorComPerfil(
                "Bruna",
                "bruna@ifsc.edu.br",
                "Inteligência Artificial"
        );

        Orientador zeroVagas = salvarOrientadorComPerfil(
                "Diego",
                "diego@ifsc.edu.br",
                "Inteligência Artificial"
        );

        umaVaga.getPerfil().setVagasDisponiveis(1);
        zeroVagas.getPerfil().setVagasDisponiveis(0);

        entityManager.flush();
        entityManager.clear();

        List<Orientador> resultado =
                orientadorRepository.findByFiltros(null, true);

        assertThat(resultado)
                .extracting(Orientador::getNome)
                .containsExactly("Bruna");
    }

    @Test
    @DisplayName("Com temVagas = false, deve se comportar como se o filtro de vagas não tivesse sido informado")
    void findByFiltros_ComTemVagasFalse_NaoDeveFiltrarPorVagas() {
        Orientador comVagas = salvarOrientadorComPerfil(
                "Elisa",
                "elisa@ifsc.edu.br",
                "Inteligência Artificial"
        );

        Orientador semVagas = salvarOrientadorComPerfil(
                "Fabio",
                "fabio@ifsc.edu.br",
                "Inteligência Artificial"
        );

        comVagas.getPerfil().setVagasDisponiveis(3);
        semVagas.getPerfil().setVagasDisponiveis(0);

        entityManager.flush();
        entityManager.clear();

        List<Orientador> resultado =
                orientadorRepository.findByFiltros(null, false);

        assertThat(resultado)
                .extracting(Orientador::getNome)
                .containsExactlyInAnyOrder("Elisa", "Fabio");
    }

    @Test
    @DisplayName("Não deve duplicar orientador com múltiplas linhas de pesquisa quando o filtro bate em apenas uma delas")
    void findByFiltros_ComMultiplasLinhasDePesquisa_NaoDeveDuplicarResultado() {
        Orientador orientador = salvarOrientadorComPerfil(
                "Gustavo",
                "gustavo@ifsc.edu.br",
                "Inteligência Artificial"
        );

        LinhaPesquisa segundaLinha = new LinhaPesquisa();
        segundaLinha.setNome("Engenharia de Software");
        segundaLinha.setPerfilOrientador(orientador.getPerfil());
        orientador.getPerfil().getLinhasPesquisa().add(segundaLinha);

        entityManager.flush();
        entityManager.clear();

        List<Orientador> resultado =
                orientadorRepository.findByFiltros("Inteligência", null);

        assertThat(resultado)
                .extracting(Orientador::getNome)
                .containsExactly("Gustavo");
    }

    @Test
    @DisplayName("Deve combinar filtro de linha de pesquisa e vagas disponíveis")
    void findByFiltros_ComLinhaEComVagas_DeveRetornarSomenteCorrespondentes() {
        Orientador comLinhaEComVagas = salvarOrientadorComPerfil(
                "Ana",
                "ana@ifsc.edu.br",
                "Inteligência Artificial"
        );

        Orientador comLinhaSemVagas = salvarOrientadorComPerfil(
                "Carlos",
                "carlos@ifsc.edu.br",
                "Inteligência Artificial"
        );

        Orientador outraLinhaComVagas = salvarOrientadorComPerfil(
                "Maria",
                "maria@ifsc.edu.br",
                "Desenvolvimento Web"
        );

        comLinhaEComVagas.getPerfil().setVagasDisponiveis(2);
        comLinhaSemVagas.getPerfil().setVagasDisponiveis(0);
        outraLinhaComVagas.getPerfil().setVagasDisponiveis(3);

        entityManager.flush();
        entityManager.clear();

        List<Orientador> resultado =
                orientadorRepository.findByFiltros("Inteligência", true);

        assertThat(resultado)
                .extracting(Orientador::getNome)
                .containsExactly("Ana");
    }
}