package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerfilProfessorControllerIntegrationTest {

    private static final String URL = "/api/v1/professores/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private TurmaTCCRepository turmaTCCRepository;

    private Orientador salvarProfessor(String nome, String email) {
        Orientador professor = new Orientador();
        professor.setNome(nome);
        professor.setEmail(email);
        professor.setDepartamento("DAELN");
        professor.setAtivo(true);
        return orientadorRepository.saveAndFlush(professor);
    }

    private void salvarTurma(String semestre, boolean vigente, Orientador responsavel) {
        turmaTCCRepository.saveAndFlush(TurmaTCC.builder()
                .semestre(semestre)
                .vigente(vigente)
                .professorResponsavel(responsavel)
                .build());
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email))
                .authorities(new SimpleGrantedAuthority(role));
    }

    @Test
    @DisplayName("Deve retornar responsavelTurma true para o professor responsável pela turma vigente")
    void buscaPerfil_ProfessorResponsavel_DeveRetornarResponsavelTurmaTrue() throws Exception {
        Orientador responsavel = salvarProfessor("Professor Responsável", "professor.responsavel@example.com");
        salvarTurma("2026/2", true, responsavel);

        mockMvc.perform(get(URL).with(tokenDe("ROLE_PROFESSOR", "professor.responsavel@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responsavel.getId()))
                .andExpect(jsonPath("$.nome").value("Professor Responsável"))
                .andExpect(jsonPath("$.email").value("professor.responsavel@example.com"))
                .andExpect(jsonPath("$.departamento").value("DAELN"))
                .andExpect(jsonPath("$.responsavelTurma").value(true));
    }

    @Test
    @DisplayName("Deve retornar responsavelTurma false para o professor que não é responsável pela turma")
    void buscaPerfil_ProfessorNaoResponsavel_DeveRetornarResponsavelTurmaFalse() throws Exception {
        Orientador responsavel = salvarProfessor("Professor Responsável", "professor.responsavel@example.com");
        salvarProfessor("Professor Orientador", "professor.orientador@example.com");
        salvarTurma("2026/2", true, responsavel);

        mockMvc.perform(get(URL).with(tokenDe("ROLE_PROFESSOR", "professor.orientador@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("professor.orientador@example.com"))
                .andExpect(jsonPath("$.responsavelTurma").value(false));
    }

    @Test
    @DisplayName("Deve retornar responsavelTurma false quando a turma sob responsabilidade do professor não é vigente")
    void buscaPerfil_ResponsavelPorTurmaNaoVigente_DeveRetornarResponsavelTurmaFalse() throws Exception {
        Orientador professor = salvarProfessor("Professor Responsável", "professor.responsavel@example.com");
        salvarTurma("2026/1", false, professor);

        mockMvc.perform(get(URL).with(tokenDe("ROLE_PROFESSOR", "professor.responsavel@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavelTurma").value(false));
    }

    @Test
    @DisplayName("Deve identificar o professor ignorando diferença de maiúsculas e minúsculas no e-mail")
    void buscaPerfil_EmailComCaixaDiferente_DeveEncontrarProfessor() throws Exception {
        Orientador responsavel = salvarProfessor("Professor Responsável", "Professor.Responsavel@example.com");
        salvarTurma("2026/2", true, responsavel);

        mockMvc.perform(get(URL).with(tokenDe("ROLE_PROFESSOR", "professor.responsavel@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavelTurma").value(true));
    }

    @Test
    @DisplayName("Deve retornar 404 quando o e-mail do token não pertence a nenhum professor cadastrado")
    void buscaPerfil_ProfessorNaoCadastrado_DeveRetornar404() throws Exception {
        mockMvc.perform(get(URL).with(tokenDe("ROLE_PROFESSOR", "desconhecido@example.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 404 quando o token não possui a claim de e-mail")
    void buscaPerfil_TokenSemEmail_DeveRetornar404() throws Exception {
        mockMvc.perform(get(URL).with(SecurityMockMvcRequestPostProcessors.jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_PROFESSOR"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 403 quando o usuário autenticado é aluno")
    void buscaPerfil_UsuarioAluno_DeveRetornar403() throws Exception {
        mockMvc.perform(get(URL).with(tokenDe("ROLE_ALUNO", "aluno@example.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 401 quando a requisição não possui token")
    void buscaPerfil_SemToken_DeveRetornar401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());
    }
}
