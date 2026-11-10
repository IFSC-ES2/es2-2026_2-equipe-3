
package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Entrega;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.StatusEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.EntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EntregaParecerIntegrationTest {

    private static final String URL = "/api/v1/entregas";
    private static final String EMAIL_ALUNO = "aluno.parecer@example.com";
    private static final String EMAIL_ORIENTADOR = "orientador.parecer@example.com";
    private static final String URL_PROJETO = "https://docs.example.com/projeto-tcc";
    private static final String URL_TCC1 = "https://docs.example.com/tcc1";
    private static final String COMENTARIO_REPROVACAO =
            "O trabalho não atende aos requisitos mínimos.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private TurmaTCCRepository turmaTCCRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    private Aluno aluno;
    private Orientador orientador;
    private TurmaTCC turma;

    @BeforeEach
    void setUp() {
        orientador = new Orientador();
        orientador.setNome("Orientador de Teste");
        orientador.setEmail(EMAIL_ORIENTADOR);
        orientador.setDepartamento("DAELN");
        orientador.setAtivo(true);
        orientadorRepository.saveAndFlush(orientador);

        aluno = alunoRepository.saveAndFlush(Aluno.builder()
                .nome("Aluno de Teste")
                .email(EMAIL_ALUNO)
                .curso("Análise e Desenvolvimento de Sistemas")
                .build());

        turma = turmaTCCRepository.saveAndFlush(TurmaTCC.builder()
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(orientador)
                .build());
    }

    @Test
    @DisplayName("GET /entregas/minhas - Deve refletir aprovação e reprovação após os pareceres")
    void painelDoAluno_AposPareceres_DeveExibirStatusAtualizados() throws Exception {
        Entrega projeto = criarEntrega(TipoEntrega.PROJETO_TCC, URL_PROJETO);
        Entrega tcc1 = criarEntrega(TipoEntrega.TCC1, URL_TCC1);

        registrarParecer(projeto.getId(), "APROVADO", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        registrarParecer(tcc1.getId(), "REPROVADO", COMENTARIO_REPROVACAO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADO"));

        mockMvc.perform(get(URL + "/minhas")
                        .with(tokenDe("ROLE_ALUNO", EMAIL_ALUNO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo",
                        hasItems("PROJETO_TCC", "TCC1")))
                .andExpect(jsonPath("$[*].status",
                        hasItems("APROVADO", "REPROVADO")))
                .andExpect(jsonPath("$[*].urlDocumento",
                        hasItems(URL_PROJETO, URL_TCC1)))
                .andExpect(jsonPath(
                        "$[?(@.tipo == 'PROJETO_TCC')].parecer.resultado",
                        hasItems("APROVADO")))
                .andExpect(jsonPath(
                        "$[?(@.tipo == 'TCC1')].parecer.resultado",
                        hasItems("REPROVADO")));
    }

    private Entrega criarEntrega(TipoEntrega tipo, String urlDocumento) {
        return entregaRepository.saveAndFlush(Entrega.builder()
                .aluno(aluno)
                .orientador(orientador)
                .turma(turma)
                .tipo(tipo)
                .status(StatusEntrega.SUBMETIDO)
                .urlDocumento(urlDocumento)
                .atrasada(false)
                .enviadoEm(Instant.parse("2026-11-09T12:00:00Z"))
                .build());
    }

    private ResultActions registrarParecer(
            Long entregaId, String resultado, String comentario) throws Exception {

        String payload = comentario == null
                ? "{\"resultado\":\"%s\"}".formatted(resultado)
                : "{\"resultado\":\"%s\",\"comentario\":\"%s\"}"
                .formatted(resultado, comentario);

        return mockMvc.perform(post(URL + "/" + entregaId + "/parecer")
                .with(tokenDe("ROLE_PROFESSOR", EMAIL_ORIENTADOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    private RequestPostProcessor tokenDe(String role, String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("email", email))
                .authorities(new SimpleGrantedAuthority(role));
    }
}
