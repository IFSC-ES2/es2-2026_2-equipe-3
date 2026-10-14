package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.LinhaPesquisa;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrientadorFiltroIntegrationTest {

    private static final String URL = "/api/v1/orientadores";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @BeforeEach
    void setUp() {
        orientadorRepository.deleteAll();
        salvar("Ana IA Com Vagas", "ana@ifsc.edu.br", 2, "Inteligência Artificial");
        salvar("Bruno IA Sem Vagas", "bruno@ifsc.edu.br", 0, "Inteligência Artificial");
        salvar("Carla Redes Com Vagas", "carla@ifsc.edu.br", 4, "Redes de Computadores");
        salvar("Diego Redes Sem Vagas", "diego@ifsc.edu.br", 0, "Redes de Computadores");
    }

    private void salvar(String nome, String email, int vagas, String... linhas) {
        Orientador orientador = new Orientador();
        orientador.setNome(nome);
        orientador.setEmail(email);
        orientador.setDepartamento("DAELN");
        orientador.setAtivo(true);

        PerfilOrientador perfil = new PerfilOrientador();
        perfil.setVagasDisponiveis(vagas);
        perfil.setBiografia("Biografia de " + nome);
        perfil.setOrientador(orientador);

        List<LinhaPesquisa> linhasPesquisa = new ArrayList<>();
        for (String nomeLinha : linhas) {
            LinhaPesquisa linha = new LinhaPesquisa();
            linha.setNome(nomeLinha);
            linha.setPerfilOrientador(perfil);
            linhasPesquisa.add(linha);
        }
        perfil.setLinhasPesquisa(linhasPesquisa);
        orientador.setPerfil(perfil);

        orientadorRepository.saveAndFlush(orientador);
    }

    private RequestPostProcessor aluno() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_ALUNO"));
    }

    @Test
    @DisplayName("Sem filtros deve retornar todos os orientadores")
    void listar_SemFiltros_DeveRetornarTodos() throws Exception {
        mockMvc.perform(get(URL).with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DisplayName("Filtro por linha de pesquisa deve retornar somente orientadores da linha")
    void listar_PorLinhaDePesquisa_DeveRetornarSomenteDaLinha() throws Exception {
        mockMvc.perform(get(URL).param("area", "Redes").with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.nome == 'Carla Redes Com Vagas')]").exists())
                .andExpect(jsonPath("$[?(@.nome == 'Diego Redes Sem Vagas')]").exists());
    }

    @Test
    @DisplayName("Filtro por linha de pesquisa deve ignorar diferença de maiúsculas e minúsculas")
    void listar_PorLinhaDePesquisa_DeveSerCaseInsensitive() throws Exception {
        mockMvc.perform(get(URL).param("area", "inteligência").with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Filtro por vagas disponíveis deve retornar somente orientadores com vagas")
    void listar_PorVagasDisponiveis_DeveRetornarSomenteComVagas() throws Exception {
        mockMvc.perform(get(URL).param("temVagas", "true").with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.vagasDisponiveis <= 0)]").isEmpty());
    }

    @Test
    @DisplayName("temVagas=false não deve restringir a listagem")
    void listar_TemVagasFalse_DeveRetornarTodos() throws Exception {
        mockMvc.perform(get(URL).param("temVagas", "false").with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DisplayName("Filtros combinados devem retornar somente a interseção dos critérios")
    void listar_FiltrosCombinados_DeveRetornarInterseccao() throws Exception {
        mockMvc.perform(get(URL)
                        .param("area", "Inteligência")
                        .param("temVagas", "true")
                        .with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana IA Com Vagas"))
                .andExpect(jsonPath("$[0].vagasDisponiveis").value(2));
    }

    @Test
    @DisplayName("Linha de pesquisa inexistente deve retornar 200 com lista vazia")
    void listar_LinhaInexistente_DeveRetornarListaVazia() throws Exception {
        mockMvc.perform(get(URL).param("area", "Robótica").with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Filtros combinados sem interseção devem retornar 200 com lista vazia")
    void listar_FiltrosCombinadosSemInterseccao_DeveRetornarListaVazia() throws Exception {
        orientadorRepository.deleteAll();
        salvar("Bruno IA Sem Vagas", "bruno@ifsc.edu.br", 0, "Inteligência Artificial");

        mockMvc.perform(get(URL)
                        .param("area", "Inteligência")
                        .param("temVagas", "true")
                        .with(aluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Requisição sem autenticação deve ser rejeitada")
    void listar_SemAutenticacao_DeveRetornar401() throws Exception {
        mockMvc.perform(get(URL).param("area", "Redes"))
                .andExpect(status().isUnauthorized());
    }
}