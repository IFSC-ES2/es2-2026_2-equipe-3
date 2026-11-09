package br.edu.ifsc.gestao_tcc.controller;

import br.edu.ifsc.gestao_tcc.event.SolicitacaoStatusChangedEvent;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PerfilOrientador;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Reproduz a condição de corrida da issue #206. Não usa {@code @Transactional} na classe porque
 * cada requisição precisa fazer o próprio commit para que o conflito ocorra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(SolicitacaoConcorrenciaIntegrationTest.ConfiguracaoBarreira.class)
class SolicitacaoConcorrenciaIntegrationTest {

    private static final String EMAIL_PROFESSOR = "professor.concorrencia@ifsc.edu.br";
    private static final String MENSAGEM_CONFLITO =
            "Os dados foram alterados por outra operação ao mesmo tempo. Atualize a página e tente novamente.";

    /**
     * Segura cada aceite depois da validação de vagas e antes do commit, até que o outro aceite
     * chegue ao mesmo ponto. Assim, as duas transações sempre leem o mesmo valor de vagas.
     */
    @TestConfiguration
    static class ConfiguracaoBarreira {

        private final CyclicBarrier barreira = new CyclicBarrier(2);

        @EventListener
        void aguardarOutroAceite(SolicitacaoStatusChangedEvent event) throws Exception {
            barreira.await(10, TimeUnit.SECONDS);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrientadorRepository orientadorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    private final List<SolicitacaoOrientacao> solicitacoesCriadas = new ArrayList<>();
    private final List<Aluno> alunosCriados = new ArrayList<>();
    private Orientador professor;

    @BeforeEach
    void limparListas() {
        solicitacoesCriadas.clear();
        alunosCriados.clear();
    }

    @AfterEach
    void removerDados() {
        solicitacaoRepository.deleteAll(solicitacoesCriadas);
        alunoRepository.deleteAll(alunosCriados);
        orientadorRepository.deleteById(professor.getId());
    }

    @Test
    @DisplayName("Com 1 vaga e 2 aceites simultâneos, um retorna 200, o outro 409 e as vagas terminam em 0")
    void aceitesSimultaneos_ComUmaVaga_SomenteUmDeveSerAceito() throws Exception {
        professor = salvarProfessor(1);
        SolicitacaoOrientacao primeira = salvarSolicitacaoPendente("primeiro");
        SolicitacaoOrientacao segunda = salvarSolicitacaoPendente("segundo");

        List<MockHttpServletResponse> respostas = aceitarEmParalelo(primeira, segunda);

        assertUmSucessoEUmConflito(respostas);
        assertThat(vagasAtuais()).isZero();
        assertThat(quantidadeDeAceitas()).isEqualTo(1);
    }

    @Test
    @DisplayName("A mesma solicitação aceita 2 vezes ao mesmo tempo consome somente uma vaga")
    void aceitesSimultaneos_DaMesmaSolicitacao_DeveConsumirUmaVaga() throws Exception {
        professor = salvarProfessor(2);
        SolicitacaoOrientacao solicitacao = salvarSolicitacaoPendente("unico");

        List<MockHttpServletResponse> respostas = aceitarEmParalelo(solicitacao, solicitacao);

        assertUmSucessoEUmConflito(respostas);
        assertThat(vagasAtuais()).isEqualTo(1);
        assertThat(quantidadeDeAceitas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Com 2 vagas e 2 aceites simultâneos, um deles também recebe 409 e pode ser repetido")
    void aceitesSimultaneos_ComDuasVagas_UmDeveSerRepetido() throws Exception {
        professor = salvarProfessor(2);
        SolicitacaoOrientacao primeira = salvarSolicitacaoPendente("primeiro");
        SolicitacaoOrientacao segunda = salvarSolicitacaoPendente("segundo");

        List<MockHttpServletResponse> respostas = aceitarEmParalelo(primeira, segunda);

        assertUmSucessoEUmConflito(respostas);
        assertThat(vagasAtuais()).isEqualTo(1);
        assertThat(quantidadeDeAceitas()).isEqualTo(1);
    }

    private List<MockHttpServletResponse> aceitarEmParalelo(
            SolicitacaoOrientacao primeira, SolicitacaoOrientacao segunda) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<MockHttpServletResponse>> aceites = List.of(aceite(primeira), aceite(segunda));
            List<MockHttpServletResponse> respostas = new ArrayList<>();
            for (Future<MockHttpServletResponse> futuro : executor.invokeAll(aceites, 30, TimeUnit.SECONDS)) {
                respostas.add(futuro.get());
            }
            respostas.sort(Comparator.comparingInt(MockHttpServletResponse::getStatus));
            return respostas;
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<MockHttpServletResponse> aceite(SolicitacaoOrientacao solicitacao) {
        return () -> mockMvc.perform(patch("/api/v1/solicitacoes/{id}/status", solicitacao.getId())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.claim("email", EMAIL_PROFESSOR))
                                .authorities(new SimpleGrantedAuthority("ROLE_PROFESSOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACEITA\"}"))
                .andReturn()
                .getResponse();
    }

    private void assertUmSucessoEUmConflito(List<MockHttpServletResponse> respostas) throws Exception {
        assertThat(respostas).extracting(MockHttpServletResponse::getStatus).containsExactly(200, 409);
        assertThat(respostas.get(1).getContentAsString(StandardCharsets.UTF_8))
                .contains("\"erro\":\"Conflito de dados\"")
                .contains(MENSAGEM_CONFLITO);
    }

    private int vagasAtuais() {
        return orientadorRepository.findById(professor.getId())
                .orElseThrow()
                .getPerfil()
                .getVagasDisponiveis();
    }

    private int quantidadeDeAceitas() {
        return solicitacaoRepository
                .findByOrientadorIdAndStatusOrderByCriadoEmDesc(professor.getId(), StatusSolicitacao.ACEITA)
                .size();
    }

    private Orientador salvarProfessor(int vagas) {
        Orientador novoProfessor = new Orientador();
        novoProfessor.setNome("Professor Concorrência");
        novoProfessor.setEmail(EMAIL_PROFESSOR);
        novoProfessor.setDepartamento("DAELN");
        novoProfessor.setAtivo(true);
        novoProfessor.setPerfil(PerfilOrientador.builder()
                .vagasDisponiveis(vagas)
                .biografia("Biografia")
                .orientador(novoProfessor)
                .build());
        return orientadorRepository.saveAndFlush(novoProfessor);
    }

    private SolicitacaoOrientacao salvarSolicitacaoPendente(String identificadorAluno) {
        Aluno aluno = alunoRepository.saveAndFlush(Aluno.builder()
                .nome("Aluno " + identificadorAluno)
                .email("aluno." + identificadorAluno + ".concorrencia@ifsc.edu.br")
                .curso("Análise e Desenvolvimento de Sistemas")
                .build());
        alunosCriados.add(aluno);

        SolicitacaoOrientacao solicitacao = solicitacaoRepository.saveAndFlush(SolicitacaoOrientacao.builder()
                .orientador(professor)
                .aluno(aluno)
                .tema("Tema de TCC em Engenharia de Software")
                .mensagem("Gostaria de desenvolver meu trabalho sob sua orientação.")
                .status(StatusSolicitacao.PENDENTE)
                .build());
        solicitacoesCriadas.add(solicitacao);
        return solicitacao;
    }
}
