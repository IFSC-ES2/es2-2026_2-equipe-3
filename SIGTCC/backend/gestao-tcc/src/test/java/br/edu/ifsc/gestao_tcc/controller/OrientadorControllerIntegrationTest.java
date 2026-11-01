package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrientadorControllerIntegrationTest {

    private static final String URL = "/api/v1/orientadores";
    private static final String EMAIL_PROFESSOR_A = "professor.orientador@example.com";
    private static final String EMAIL_PROFESSOR_B = "professor.responsavel@example.com";
    private static final String EMAIL_ALUNO = "aluno@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    private Orientador professorA;
    private Orientador professorB;

    @BeforeEach
    void setUp() {
        professorA = salvarProfessor("Professor Orientador", EMAIL_PROFESSOR_A);
        professorB = salvarProfessor("Professor Responsável", EMAIL_PROFESSOR_B);
    }

    private Orientador salvarProfessor(String nome, String email) {
        Orientador professor = new Orientador();
        professor.setNome(nome);
        professor.setEmail(email);
        professor.setDepartamento("DAELN");
        professor.setAtivo(true);
        professor.setPerfil(PerfilOrientador.builder()
                .vagasDisponiveis(3)
                .biografia("Biografia original")
                .orientador(professor)
                .build());
        return orientadorRepository.saveAndFlush(professor);
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email))
                .authorities(new SimpleGrantedAuthority(role));
    }

    private Orientador recarregar(Orientador orientador) {
        return orientadorRepository.findById(orientador.getId()).orElseThrow();
    }

    // --- Aluno: leitura liberada, escrita bloqueada ---

    @Test
    @DisplayName("GET /orientadores - Aluno deve consultar o catálogo (200 OK)")
    void listar_Aluno_DeveRetornar200() throws Exception {
        mockMvc.perform(get(URL).with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /orientadores/{id} - Aluno deve consultar um orientador (200 OK)")
    void buscarPorId_Aluno_DeveRetornar200() throws Exception {
        mockMvc.perform(get(URL + "/" + professorA.getId()).with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_PROFESSOR_A));
    }

    @Test
    @DisplayName("POST /orientadores - Aluno não pode cadastrar orientador (403 Forbidden)")
    void cadastrar_Aluno_DeveRetornar403() throws Exception {
        String payload = """
                {
                  "nome": "Orientador Falso",
                  "email": "falso@example.com",
                  "linhasDePesquisa": ["IA"],
                  "vagasDisponiveis": 3
                }
                """;

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        assertThat(orientadorRepository.existsByEmail("falso@example.com")).isFalse();
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Aluno não pode editar orientador (403 Forbidden)")
    void atualizar_Aluno_DeveRetornar403() throws Exception {
        mockMvc.perform(patch(URL + "/" + professorA.getId())
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vagasDisponiveis\": 0}"))
                .andExpect(status().isForbidden());

        assertThat(recarregar(professorA).getPerfil().getVagasDisponiveis()).isEqualTo(3);
    }

    @Test
    @DisplayName("DELETE /orientadores/{id} - Aluno não pode desativar orientador (403 Forbidden)")
    void deletar_Aluno_DeveRetornar403() throws Exception {
        mockMvc.perform(delete(URL + "/" + professorA.getId()).with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isForbidden());

        assertThat(recarregar(professorA).getAtivo()).isTrue();
    }

    // --- Professor: só altera o próprio cadastro ---

    @Test
    @DisplayName("POST /orientadores - Professor pode cadastrar orientador (201 Created)")
    void cadastrar_Professor_DeveRetornar201() throws Exception {
        String payload = """
                {
                  "nome": "Novo Orientador",
                  "email": "novo.orientador@example.com",
                  "linhasDePesquisa": ["IA"],
                  "vagasDisponiveis": 2
                }
                """;

        mockMvc.perform(post(URL)
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Professor não pode editar o cadastro de outro professor (403 Forbidden)")
    void atualizar_ProfessorEditandoOutro_DeveRetornar403() throws Exception {
        mockMvc.perform(patch(URL + "/" + professorB.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vagasDisponiveis\": 0, \"biografia\": \"Alterada por terceiro\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));

        Orientador b = recarregar(professorB);
        assertThat(b.getPerfil().getVagasDisponiveis()).isEqualTo(3);
        assertThat(b.getPerfil().getBiografia()).isEqualTo("Biografia original");
    }

    @Test
    @DisplayName("DELETE /orientadores/{id} - Professor não pode desativar outro professor (403 Forbidden)")
    void deletar_ProfessorDesativandoOutro_DeveRetornar403() throws Exception {
        mockMvc.perform(delete(URL + "/" + professorB.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A)))
                .andExpect(status().isForbidden());

        assertThat(recarregar(professorB).getAtivo()).isTrue();
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Professor pode editar o próprio cadastro (200 OK)")
    void atualizar_ProfessorEditandoProprio_DeveRetornar200() throws Exception {
        mockMvc.perform(patch(URL + "/" + professorA.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vagasDisponiveis\": 5, \"biografia\": \"Nova biografia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professorA.getId()))
                .andExpect(jsonPath("$.vagasDisponiveis").value(5))
                .andExpect(jsonPath("$.biografia").value("Nova biografia"));
    }

    @Test
    @DisplayName("DELETE /orientadores/{id} - Professor pode desativar o próprio cadastro (204 No Content)")
    void deletar_ProfessorDesativandoProprio_DeveRetornar204() throws Exception {
        mockMvc.perform(delete(URL + "/" + professorA.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A)))
                .andExpect(status().isNoContent());

        assertThat(recarregar(professorA).getAtivo()).isFalse();
    }

    @Test
    @DisplayName("PATCH /orientadores/{id} - Professor não pode trocar o próprio e-mail (422 Unprocessable Entity)")
    void atualizar_ProfessorTrocandoEmail_DeveRetornar422() throws Exception {
        mockMvc.perform(patch(URL + "/" + professorA.getId())
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"outro.email@example.com\"}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detalhes").value("O e-mail do orientador não pode ser alterado."));

        assertThat(recarregar(professorA).getEmail()).isEqualTo(EMAIL_PROFESSOR_A);
    }
}
