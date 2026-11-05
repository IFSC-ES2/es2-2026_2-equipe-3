package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.EntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SubmissaoEntregaResultado;
import br.edu.ifsc.gestao_tcc.exception.AcessoNegadoException;
import br.edu.ifsc.gestao_tcc.exception.ConflitoDeEstadoException;
import br.edu.ifsc.gestao_tcc.exception.RegraDeNegocioException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Entrega;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusEntrega;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.EntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntregaServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    /** Agora fixo nos testes: 15/10/2026, 09:00 no fuso de São Paulo. */
    private static final Instant AGORA = Instant.parse("2026-10-15T12:00:00Z");
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 15);

    private static final String EMAIL_ALUNO = "aluno@ifsc.edu.br";
    private static final String URL_DOCUMENTO = "https://docs.google.com/document/d/123/edit";
    private static final String NOVA_URL = "https://github.com/org/tcc";
    private static final String MENSAGEM_ACESSO_NEGADO =
            "Somente alunos com orientador vinculado (solicitação aceita) podem submeter entregas.";

    @Mock
    private EntregaRepository entregaRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @Mock
    private TurmaTCCRepository turmaTCCRepository;

    @Mock
    private PrazoEntregaRepository prazoEntregaRepository;

    private EntregaService service;

    private Aluno aluno;
    private Orientador orientador;
    private TurmaTCC turma;
    private final EntregaRequestDTO requisicao = new EntregaRequestDTO(TipoEntrega.PROJETO_TCC, URL_DOCUMENTO);

    @BeforeEach
    void setUp() {
        service = servicoCom(Clock.fixed(AGORA, FUSO));

        aluno = Aluno.builder()
                .id(7L)
                .nome("Aluno de Teste")
                .email(EMAIL_ALUNO)
                .curso("ADS")
                .build();

        orientador = new Orientador();
        orientador.setId(1L);
        orientador.setNome("Professor Orientador");
        orientador.setEmail("orientador@ifsc.edu.br");

        turma = TurmaTCC.builder()
                .id(10L)
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(orientador)
                .build();
    }

    private EntregaService servicoCom(Clock clock) {
        return new EntregaService(entregaRepository, alunoRepository, solicitacaoRepository,
                turmaTCCRepository, prazoEntregaRepository, clock);
    }

    private void comVinculoAceito() {
        SolicitacaoOrientacao solicitacao = SolicitacaoOrientacao.builder()
                .aluno(aluno)
                .orientador(orientador)
                .status(StatusSolicitacao.ACEITA)
                .build();

        when(alunoRepository.findByEmail(EMAIL_ALUNO)).thenReturn(Optional.of(aluno));
        when(solicitacaoRepository.findFirstByAlunoIdAndStatusOrderByCriadoEmDesc(7L, StatusSolicitacao.ACEITA))
                .thenReturn(Optional.of(solicitacao));
    }

    private void comPrazo(LocalDate dataLimite) {
        when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.of(turma));
        when(prazoEntregaRepository.findByTurmaIdAndTipo(10L, TipoEntrega.PROJETO_TCC))
                .thenReturn(Optional.of(PrazoEntrega.builder()
                        .turma(turma)
                        .tipo(TipoEntrega.PROJETO_TCC)
                        .dataLimite(dataLimite)
                        .build()));
    }

    private void semEntregaExistente() {
        when(entregaRepository.findByAlunoIdAndTurmaIdAndTipo(7L, 10L, TipoEntrega.PROJETO_TCC))
                .thenReturn(Optional.empty());
        when(entregaRepository.saveAndFlush(any(Entrega.class))).thenAnswer(invocacao -> {
            Entrega salva = invocacao.getArgument(0);
            salva.setId(100L);
            return salva;
        });
    }

    private Entrega comEntregaExistente(StatusEntrega status) {
        Entrega existente = Entrega.builder()
                .id(100L)
                .aluno(aluno)
                .orientador(orientador)
                .turma(turma)
                .tipo(TipoEntrega.PROJETO_TCC)
                .status(status)
                .urlDocumento(URL_DOCUMENTO)
                .atrasada(false)
                .enviadoEm(Instant.parse("2026-10-01T10:00:00Z"))
                .build();

        when(entregaRepository.findByAlunoIdAndTurmaIdAndTipo(7L, 10L, TipoEntrega.PROJETO_TCC))
                .thenReturn(Optional.of(existente));
        return existente;
    }

    @Nested
    @DisplayName("Primeira submissão")
    class PrimeiraSubmissao {

        @Test
        @DisplayName("Deve criar a entrega como SUBMETIDO com os dados do vínculo e da turma vigente")
        void submeter_PrimeiraVez_DeveCriarEntrega() {
            comVinculoAceito();
            comPrazo(HOJE.plusDays(10));
            semEntregaExistente();

            SubmissaoEntregaResultado resultado = service.submeter(EMAIL_ALUNO, requisicao);

            assertThat(resultado.criada()).isTrue();
            assertThat(resultado.entrega().id()).isEqualTo(100L);
            assertThat(resultado.entrega().semestre()).isEqualTo("2026/2");
            assertThat(resultado.entrega().tipo()).isEqualTo(TipoEntrega.PROJETO_TCC);
            assertThat(resultado.entrega().descricaoTipo()).isEqualTo("Projeto de TCC");
            assertThat(resultado.entrega().urlDocumento()).isEqualTo(URL_DOCUMENTO);
            assertThat(resultado.entrega().status()).isEqualTo(StatusEntrega.SUBMETIDO);
            assertThat(resultado.entrega().enviadoEm()).isEqualTo(AGORA);
            assertThat(resultado.entrega().aluno().id()).isEqualTo(7L);
            assertThat(resultado.entrega().orientador().id()).isEqualTo(1L);
            assertThat(resultado.entrega().parecer()).isNull();
        }

        @Test
        @DisplayName("Deve usar o orientador da solicitação aceita retornada pela busca")
        void submeter_DeveUsarOrientadorDoVinculo() {
            Orientador orientadorMaisRecente = new Orientador();
            orientadorMaisRecente.setId(2L);
            orientadorMaisRecente.setNome("Orientador Mais Recente");
            orientadorMaisRecente.setEmail("recente@ifsc.edu.br");

            when(alunoRepository.findByEmail(EMAIL_ALUNO)).thenReturn(Optional.of(aluno));
            when(solicitacaoRepository.findFirstByAlunoIdAndStatusOrderByCriadoEmDesc(7L, StatusSolicitacao.ACEITA))
                    .thenReturn(Optional.of(SolicitacaoOrientacao.builder()
                            .aluno(aluno)
                            .orientador(orientadorMaisRecente)
                            .status(StatusSolicitacao.ACEITA)
                            .build()));
            comPrazo(HOJE.plusDays(10));
            semEntregaExistente();

            SubmissaoEntregaResultado resultado = service.submeter(EMAIL_ALUNO, requisicao);

            assertThat(resultado.entrega().orientador().id()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Deve registrar enviadoEm sem frações de segundo, como define o contrato")
        void submeter_DeveTruncarEnviadoEmParaSegundos() {
            service = servicoCom(Clock.fixed(Instant.parse("2026-10-15T12:00:00.987654Z"), FUSO));
            comVinculoAceito();
            comPrazo(HOJE.plusDays(10));
            semEntregaExistente();

            SubmissaoEntregaResultado resultado = service.submeter(EMAIL_ALUNO, requisicao);

            assertThat(resultado.entrega().enviadoEm()).isEqualTo(AGORA);
        }

        @Test
        @DisplayName("Deve responder com 409 quando outra submissão simultânea violar a constraint única")
        void submeter_ViolacaoDeUnicidade_DeveLancarConflito() {
            comVinculoAceito();
            comPrazo(HOJE.plusDays(10));
            when(entregaRepository.findByAlunoIdAndTurmaIdAndTipo(7L, 10L, TipoEntrega.PROJETO_TCC))
                    .thenReturn(Optional.empty());
            when(entregaRepository.saveAndFlush(any(Entrega.class)))
                    .thenThrow(new DataIntegrityViolationException("uk_entrega_aluno_turma_tipo"));

            ConflitoDeEstadoException ex = assertThrows(ConflitoDeEstadoException.class,
                    () -> service.submeter(EMAIL_ALUNO, requisicao));

            assertThat(ex.getMessage()).isEqualTo("Já existe uma entrega para Projeto de TCC com status SUBMETIDO. "
                    + "O reenvio só é permitido quando o status for AJUSTES_NECESSARIOS.");
        }
    }

    @Nested
    @DisplayName("Cálculo de atraso")
    class CalculoDeAtraso {

        @Test
        @DisplayName("Não deve marcar atraso antes da data limite")
        void submeter_AntesDoPrazo_NaoDeveEstarAtrasada() {
            comVinculoAceito();
            comPrazo(HOJE.plusDays(1));
            semEntregaExistente();

            assertThat(service.submeter(EMAIL_ALUNO, requisicao).entrega().atrasada()).isFalse();
        }

        @Test
        @DisplayName("Não deve marcar atraso no próprio dia limite")
        void submeter_NoDiaLimite_NaoDeveEstarAtrasada() {
            comVinculoAceito();
            comPrazo(HOJE);
            semEntregaExistente();

            assertThat(service.submeter(EMAIL_ALUNO, requisicao).entrega().atrasada()).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar o envio e marcar atraso depois da data limite")
        void submeter_DepoisDoPrazo_DeveAceitarComAtraso() {
            comVinculoAceito();
            comPrazo(HOJE.minusDays(1));
            semEntregaExistente();

            SubmissaoEntregaResultado resultado = service.submeter(EMAIL_ALUNO, requisicao);

            assertThat(resultado.criada()).isTrue();
            assertThat(resultado.entrega().atrasada()).isTrue();
        }

        @Test
        @DisplayName("Deve usar o fuso da instituição: 02:00 UTC do dia 16 ainda é dia 15 em São Paulo")
        void submeter_ViradaDeDiaEmUtc_DeveConsiderarFusoDaInstituicao() {
            service = servicoCom(Clock.fixed(Instant.parse("2026-11-16T02:00:00Z"), FUSO));
            comVinculoAceito();
            comPrazo(LocalDate.of(2026, 11, 15));
            semEntregaExistente();

            assertThat(service.submeter(EMAIL_ALUNO, requisicao).entrega().atrasada()).isFalse();
        }
    }

    @Nested
    @DisplayName("Reenvio e duplicidade")
    class ReenvioEDuplicidade {

        @Test
        @DisplayName("Deve atualizar a entrega existente quando o status for AJUSTES_NECESSARIOS")
        void submeter_ComAjustesNecessarios_DeveAtualizarEntrega() {
            comVinculoAceito();
            comPrazo(HOJE.minusDays(1));
            Entrega existente = comEntregaExistente(StatusEntrega.AJUSTES_NECESSARIOS);
            when(entregaRepository.save(existente)).thenReturn(existente);

            SubmissaoEntregaResultado resultado = service.submeter(EMAIL_ALUNO,
                    new EntregaRequestDTO(TipoEntrega.PROJETO_TCC, NOVA_URL));

            assertThat(resultado.criada()).isFalse();
            assertThat(resultado.entrega().id()).isEqualTo(100L);
            assertThat(resultado.entrega().urlDocumento()).isEqualTo(NOVA_URL);
            assertThat(resultado.entrega().status()).isEqualTo(StatusEntrega.SUBMETIDO);
            assertThat(resultado.entrega().atrasada()).isTrue();
            assertThat(resultado.entrega().enviadoEm()).isEqualTo(AGORA);
        }

        @ParameterizedTest
        @EnumSource(value = StatusEntrega.class, names = {"SUBMETIDO", "APROVADO", "REPROVADO"})
        @DisplayName("Deve bloquear nova submissão quando o status não permitir reenvio")
        void submeter_ComStatusBloqueado_DeveLancarConflito(StatusEntrega status) {
            comVinculoAceito();
            comPrazo(HOJE.plusDays(10));
            Entrega existente = comEntregaExistente(status);

            ConflitoDeEstadoException ex = assertThrows(ConflitoDeEstadoException.class,
                    () -> service.submeter(EMAIL_ALUNO, new EntregaRequestDTO(TipoEntrega.PROJETO_TCC, NOVA_URL)));

            assertThat(ex.getMessage()).isEqualTo("Já existe uma entrega para Projeto de TCC com status " + status
                    + ". O reenvio só é permitido quando o status for AJUSTES_NECESSARIOS.");
            assertThat(existente.getUrlDocumento()).isEqualTo(URL_DOCUMENTO);
            verify(entregaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Acesso negado")
    class AcessoNegado {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Deve negar quando o token não traz e-mail")
        void submeter_SemEmail_DeveLancarAcessoNegado(String email) {
            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> service.submeter(email, requisicao));

            assertThat(ex.getMessage()).isEqualTo(MENSAGEM_ACESSO_NEGADO);
            verify(alunoRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("Deve negar quando o aluno não está cadastrado")
        void submeter_AlunoSemCadastro_DeveLancarAcessoNegado() {
            when(alunoRepository.findByEmail(EMAIL_ALUNO)).thenReturn(Optional.empty());

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> service.submeter(EMAIL_ALUNO, requisicao));

            assertThat(ex.getMessage()).isEqualTo(MENSAGEM_ACESSO_NEGADO);
        }

        @Test
        @DisplayName("Deve negar quando o aluno não possui solicitação aceita")
        void submeter_SemSolicitacaoAceita_DeveLancarAcessoNegado() {
            when(alunoRepository.findByEmail(EMAIL_ALUNO)).thenReturn(Optional.of(aluno));
            when(solicitacaoRepository.findFirstByAlunoIdAndStatusOrderByCriadoEmDesc(7L, StatusSolicitacao.ACEITA))
                    .thenReturn(Optional.empty());

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> service.submeter(EMAIL_ALUNO, requisicao));

            assertThat(ex.getMessage()).isEqualTo(MENSAGEM_ACESSO_NEGADO);
            verify(entregaRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("Turma e prazo")
    class TurmaEPrazo {

        @Test
        @DisplayName("Deve rejeitar quando não há turma vigente")
        void submeter_SemTurmaVigente_DeveLancarRegraDeNegocio() {
            comVinculoAceito();
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.empty());

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.submeter(EMAIL_ALUNO, requisicao));

            assertThat(ex.getMessage()).isEqualTo("Não há turma de TCC vigente cadastrada para receber entregas.");
        }

        @Test
        @DisplayName("Deve rejeitar quando a etapa não tem prazo cadastrado na turma vigente")
        void submeter_SemPrazo_DeveLancarRegraDeNegocio() {
            comVinculoAceito();
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.of(turma));
            when(prazoEntregaRepository.findByTurmaIdAndTipo(10L, TipoEntrega.PROJETO_TCC))
                    .thenReturn(Optional.empty());

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.submeter(EMAIL_ALUNO, requisicao));

            assertThat(ex.getMessage()).isEqualTo("Não há prazo cadastrado para Projeto de TCC na turma vigente.");
        }
    }
}
