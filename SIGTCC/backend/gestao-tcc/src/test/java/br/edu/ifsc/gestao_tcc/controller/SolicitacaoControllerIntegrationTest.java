package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitacaoControllerIntegrationTest {

    private static final String EMAIL_PROFESSOR_A = "professor.destinatario@ifsc.edu.br";
    private static final String EMAIL_PROFESSOR_B = "outro.professor@ifsc.edu.br";
    private static final String EMAIL_ALUNO = "aluno@aluno.ifsc.edu.br";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    private Orientador professorA;
    private Orientador professorB;
    private SolicitacaoOrientacao solicitacaoDeProfessorA;

    @BeforeEach
    void setUp() {
        professorA = salvarProfessor("Professor Destinatário", EMAIL_PROFESSOR_A, 2);
        professorB = salvarProfessor("Outro Professor", EMAIL_PROFESSOR_B, 2);
        Aluno aluno = salvarAluno("Aluno Teste", EMAIL_ALUNO);
        solicitacaoDeProfessorA = salvarSolicitacaoPendente(professorA, aluno);
    }

    private Orientador salvarProfessor(String nome, String email, int vagas) {
        Orientador professor = new Orientador();
        professor.setNome(nome);
        professor.setEmail(email);
        professor.setDepartamento("DAELN");
        professor.setAtivo(true);
        professor.setPerfil(PerfilOrientador.builder()
                .vagasDisponiveis(vagas)
                .biografia("Biografia de " + nome)
                .orientador(professor)
                .build());
        return orientadorRepository.saveAndFlush(professor);
    }

    private Aluno salvarAluno(String nome, String email) {
        Aluno aluno = Aluno.builder()
                .nome(nome)
                .email(email)
                .curso("Análise e Desenvolvimento de Sistemas")
                .build();
        return alunoRepository.saveAndFlush(aluno);
    }

    private SolicitacaoOrientacao salvarSolicitacaoPendente(Orientador orientador, Aluno aluno) {
        SolicitacaoOrientacao solicitacao = SolicitacaoOrientacao.builder()
                .orientador(orientador)
                .aluno(aluno)
                .tema("Tema de TCC em Engenharia de Software")
                .mensagem("Gostaria de desenvolver meu trabalho sob sua orientação.")
                .status(StatusSolicitacao.PENDENTE)
                .build();
        return solicitacaoRepository.saveAndFlush(solicitacao);
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email))
                .authorities(new SimpleGrantedAuthority(role));
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Aluno não pode aceitar solicitação (403 Forbidden)")
    void atualizarStatus_AlunoAceitando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch("/api/v1/solicitacoes/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andExpect(status().isForbidden());

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Aluno não pode recusar solicitação (403 Forbidden)")
    void atualizarStatus_AlunoRecusando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch("/api/v1/solicitacoes/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RECUSADA\", \"justificativa\": \"Tentativa indevida pelo aluno.\"}"))
                .andExpect(status().isForbidden());

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Outro professor não pode aceitar solicitação alheia (403 Forbidden)")
    void atualizarStatus_OutroProfessorAceitando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch("/api/v1/solicitacoes/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_B))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"))
                .andExpect(jsonPath("$.detalhes").value(
                        "Somente o orientador destinatário pode aceitar ou recusar esta solicitação."));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
        Orientador destinatario = orientadorRepository.findById(professorA.getId()).orElseThrow();
        assertThat(destinatario.getPerfil().getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Outro professor não pode recusar solicitação alheia (403 Forbidden)")
    void atualizarStatus_OutroProfessorRecusando_DeveRetornar403() throws Exception {
        mockMvc.perform(patch("/api/v1/solicitacoes/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_B))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RECUSADA\", \"justificativa\": \"Não tenho vagas neste semestre.\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.PENDENTE);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Orientador destinatário pode aceitar solicitação e decrementar vagas (200 OK)")
    void atualizarStatus_OrientadorDestinatarioAceitando_DeveRetornar200EDecrementarVagas() throws Exception {
        mockMvc.perform(patch("/api/v1/solicitacoes/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitacaoDeProfessorA.getId()))
                .andExpect(jsonPath("$.status").value("ACEITA"))
                .andExpect(jsonPath("$.orientador.vagasDisponiveis").value(1));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.ACEITA);
    }

    @Test
    @DisplayName("PATCH /solicitacoes/{id}/status - Orientador destinatário pode recusar solicitação com justificativa (200 OK)")
    void atualizarStatus_OrientadorDestinatarioRecusando_DeveRetornar200() throws Exception {
        mockMvc.perform(patch("/api/v1/solicitacoes/" + solicitacaoDeProfessorA.getId() + "/status")
                        .with(tokenDe("ROLE_PROFESSOR", EMAIL_PROFESSOR_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"RECUSADA\", \"justificativa\": \"Sem disponibilidade no momento.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitacaoDeProfessorA.getId()))
                .andExpect(jsonPath("$.status").value("RECUSADA"))
                .andExpect(jsonPath("$.justificativa").value("Sem disponibilidade no momento."));

        SolicitacaoOrientacao persistida = solicitacaoRepository.findById(solicitacaoDeProfessorA.getId()).orElseThrow();
        assertThat(persistida.getStatus()).isEqualTo(StatusSolicitacao.RECUSADA);
    }
}

