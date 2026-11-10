package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.EntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.EntregaResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.ParecerRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SubmissaoEntregaResultado;
import br.edu.ifsc.gestao_tcc.exception.AcessoNegadoException;
import br.edu.ifsc.gestao_tcc.exception.ConflitoDeEstadoException;
import br.edu.ifsc.gestao_tcc.exception.RegraDeNegocioException;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.model.Aluno;
import br.edu.ifsc.gestao_tcc.model.Entrega;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.Parecer;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.SolicitacaoOrientacao;
import br.edu.ifsc.gestao_tcc.model.StatusEntrega;
import br.edu.ifsc.gestao_tcc.model.StatusSolicitacao;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.EntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.ParecerRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
    private static final String EMAIL_ORIENTADOR = "orientador@ifsc.edu.br";
    private static final String URL_DOCUMENTO = "https://docs.google.com/document/d/123/edit";
    private static final String NOVA_URL = "https://github.com/org/tcc";
    private static final String COMENTARIO = "Detalhar melhor a seção de metodologia.";
    private static final String MENSAGEM_ACESSO_NEGADO =
            "Somente alunos com orientador vinculado (solicitação aceita) podem submeter entregas.";

    @Mock
    private EntregaRepository entregaRepository;

    @Mock
    private ParecerRepository parecerRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private OrientadorRepository orientadorRepository;

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
        orientador.setEmail(EMAIL_ORIENTADOR);

        turma = TurmaTCC.builder()
                .id(10L)
                .semestre("2026/2")
                .vigente(true)
                .professorResponsavel(orientador)
                .build();
    }

    private EntregaService servicoCom(Clock clock) {
        return new EntregaService(entregaRepository, parecerRepository, alunoRepository, orientadorRepository,
                solicitacaoRepository,
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

    private Entrega entregaSalva(TipoEntrega tipo, StatusEntrega status) {
        return Entrega.builder()
                .id(100L)
                .aluno(aluno)
                .orientador(orientador)
                .turma(turma)
                .tipo(tipo)
                .status(status)
                .urlDocumento(URL_DOCUMENTO)
                .atrasada(true)
                .enviadoEm(AGORA)
                .build();
    }

    @Nested
    @DisplayName("Consulta por identificador")
    class BuscarPorId {

        @ParameterizedTest
        @ValueSource(strings = {EMAIL_ALUNO, EMAIL_ORIENTADOR, "ALUNO@IFSC.EDU.BR", "Orientador@Ifsc.Edu.Br"})
        @DisplayName("Deve retornar os dados da entrega ao aluno autor e ao orientador vinculado")
        void buscarPorId_AutorOuOrientador_DeveRetornarEntrega(String email) {
            when(entregaRepository.findById(100L))
                    .thenReturn(Optional.of(entregaSalva(TipoEntrega.PROJETO_TCC, StatusEntrega.SUBMETIDO)));

            EntregaResponseDTO resposta = service.buscarPorId(email, 100L);

            assertThat(resposta.id()).isEqualTo(100L);
            assertThat(resposta.urlDocumento()).isEqualTo(URL_DOCUMENTO);
            assertThat(resposta.status()).isEqualTo(StatusEntrega.SUBMETIDO);
            assertThat(resposta.atrasada()).isTrue();
            assertThat(resposta.enviadoEm()).isEqualTo(AGORA);
            assertThat(resposta.parecer()).isNull();
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "outro.aluno@ifsc.edu.br", "outro.professor@ifsc.edu.br"})
        @DisplayName("Deve negar o acesso a quem não é o aluno autor nem o orientador vinculado")
        void buscarPorId_SemVinculo_DeveLancarAcessoNegado(String email) {
            when(entregaRepository.findById(100L))
                    .thenReturn(Optional.of(entregaSalva(TipoEntrega.PROJETO_TCC, StatusEntrega.SUBMETIDO)));

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> service.buscarPorId(email, 100L));

            assertThat(ex.getMessage()).isEqualTo(
                    "Acesso permitido apenas ao aluno autor ou ao orientador vinculado a esta entrega.");
        }

        @Test
        @DisplayName("Deve lançar não encontrado quando a entrega não existe")
        void buscarPorId_Inexistente_DeveLancarNaoEncontrado() {
            when(entregaRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.buscarPorId(EMAIL_ALUNO, 99L));

            assertThat(ex.getMessage()).isEqualTo("Entrega com identificador 99 não foi encontrada.");
        }
    }

    @Nested
    @DisplayName("Listagem do orientador")
    class ListarDoOrientador {

        @Test
        @DisplayName("Sem filtro, deve listar todas as entregas do orientador")
        void listarDoOrientador_SemStatus_DeveListarTodas() {
            when(orientadorRepository.findByEmailIgnoreCase(EMAIL_ORIENTADOR)).thenReturn(Optional.of(orientador));
            when(entregaRepository.findByOrientadorIdOrderByEnviadoEmDesc(1L))
                    .thenReturn(List.of(entregaSalva(TipoEntrega.PROJETO_TCC, StatusEntrega.APROVADO)));

            List<EntregaResponseDTO> resposta = service.listarDoOrientador(EMAIL_ORIENTADOR, null);

            assertThat(resposta).extracting(EntregaResponseDTO::status).containsExactly(StatusEntrega.APROVADO);
            verify(entregaRepository, never()).findByOrientadorIdAndStatusOrderByEnviadoEmDesc(any(), any());
        }

        @Test
        @DisplayName("Com filtro, deve consultar pelo status informado")
        void listarDoOrientador_ComStatus_DeveFiltrar() {
            when(orientadorRepository.findByEmailIgnoreCase(EMAIL_ORIENTADOR)).thenReturn(Optional.of(orientador));
            when(entregaRepository.findByOrientadorIdAndStatusOrderByEnviadoEmDesc(1L, StatusEntrega.SUBMETIDO))
                    .thenReturn(List.of(entregaSalva(TipoEntrega.TCC1, StatusEntrega.SUBMETIDO)));

            List<EntregaResponseDTO> resposta = service.listarDoOrientador(EMAIL_ORIENTADOR, StatusEntrega.SUBMETIDO);

            assertThat(resposta).extracting(EntregaResponseDTO::tipo).containsExactly(TipoEntrega.TCC1);
            verify(entregaRepository, never()).findByOrientadorIdOrderByEnviadoEmDesc(any());
        }

        @Test
        @DisplayName("Deve lançar não encontrado quando o e-mail não pertence a um orientador")
        void listarDoOrientador_Desconhecido_DeveLancarNaoEncontrado() {
            when(orientadorRepository.findByEmailIgnoreCase("desconhecido@example.com"))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.listarDoOrientador("desconhecido@example.com", null));

            assertThat(ex.getMessage()).isEqualTo("Orientador com e-mail desconhecido@example.com não foi encontrado.");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Deve lançar não encontrado sem consultar o banco quando o token não traz e-mail")
        void listarDoOrientador_SemEmail_DeveLancarNaoEncontrado(String email) {
            assertThrows(ResourceNotFoundException.class, () -> service.listarDoOrientador(email, null));

            verify(orientadorRepository, never()).findByEmailIgnoreCase(any());
        }
    }

    @Nested
    @DisplayName("Listagem do aluno")
    class ListarDoAluno {

        @Test
        @DisplayName("Deve listar as entregas do aluno autenticado")
        void listarDoAluno_ComEntregas_DeveListar() {
            when(alunoRepository.findByEmail(EMAIL_ALUNO)).thenReturn(Optional.of(aluno));
            when(entregaRepository.findByAlunoIdOrderByTipoAsc(7L))
                    .thenReturn(List.of(entregaSalva(TipoEntrega.PROJETO_TCC, StatusEntrega.SUBMETIDO)));

            assertThat(service.listarDoAluno(EMAIL_ALUNO))
                    .extracting(EntregaResponseDTO::tipo)
                    .containsExactly(TipoEntrega.PROJETO_TCC);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando o aluno ainda não tem entregas")
        void listarDoAluno_SemEntregas_DeveRetornarVazio() {
            when(alunoRepository.findByEmail(EMAIL_ALUNO)).thenReturn(Optional.of(aluno));
            when(entregaRepository.findByAlunoIdOrderByTipoAsc(7L)).thenReturn(List.of());

            assertThat(service.listarDoAluno(EMAIL_ALUNO)).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia, e não erro, quando o aluno não está cadastrado (D6)")
        void listarDoAluno_SemCadastro_DeveRetornarVazio() {
            when(alunoRepository.findByEmail(EMAIL_ALUNO)).thenReturn(Optional.empty());

            assertThat(service.listarDoAluno(EMAIL_ALUNO)).isEmpty();
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Deve retornar lista vazia sem consultar o banco quando o token não traz e-mail")
        void listarDoAluno_SemEmail_DeveRetornarVazio(String email) {
            assertThat(service.listarDoAluno(email)).isEmpty();

            verify(alunoRepository, never()).findByEmail(any());
        }
    }

    private Entrega comEntregaParaParecer(StatusEntrega status) {
        Entrega entrega = entregaSalva(TipoEntrega.PROJETO_TCC, status);
        when(entregaRepository.findByIdParaAtualizacao(100L)).thenReturn(Optional.of(entrega));
        return entrega;
    }

    private void comSalvamentoDeParecer() {
        when(parecerRepository.save(any(Parecer.class))).thenAnswer(invocacao -> {
            Parecer salvo = invocacao.getArgument(0);
            salvo.setId(50L);
            return salvo;
        });
    }

    private void verificarQueNadaFoiGravado() {
        verify(parecerRepository, never()).save(any());
        verify(entregaRepository, never()).save(any());
    }

    @Nested
    @DisplayName("Registro de parecer")
    class RegistrarParecer {

        @ParameterizedTest
        @EnumSource(value = StatusEntrega.class, names = {"APROVADO", "AJUSTES_NECESSARIOS", "REPROVADO"})
        @DisplayName("Deve gravar o parecer e atualizar o status da entrega")
        void registrarParecer_ComResultadoValido_DeveGravarEAtualizarStatus(StatusEntrega resultado) {
            Entrega entrega = comEntregaParaParecer(StatusEntrega.SUBMETIDO);
            comSalvamentoDeParecer();

            EntregaResponseDTO resposta = service.registrarParecer(EMAIL_ORIENTADOR, 100L,
                    new ParecerRequestDTO(resultado, COMENTARIO));

            ArgumentCaptor<Parecer> captor = ArgumentCaptor.forClass(Parecer.class);
            verify(parecerRepository).save(captor.capture());
            assertThat(captor.getValue().getEntrega()).isSameAs(entrega);
            assertThat(captor.getValue().getOrientador()).isSameAs(orientador);
            assertThat(captor.getValue().getResultado()).isEqualTo(resultado);
            assertThat(captor.getValue().getComentario()).isEqualTo(COMENTARIO);
            assertThat(captor.getValue().getRegistradoEm()).isEqualTo(AGORA);

            verify(entregaRepository).save(entrega);
            assertThat(entrega.getStatus()).isEqualTo(resultado);

            assertThat(resposta.status()).isEqualTo(resultado);
            assertThat(resposta.parecer().id()).isEqualTo(50L);
            assertThat(resposta.parecer().resultado()).isEqualTo(resultado);
            assertThat(resposta.parecer().comentario()).isEqualTo(COMENTARIO);
            assertThat(resposta.parecer().registradoEm()).isEqualTo(AGORA);
        }

        @Test
        @DisplayName("Deve identificar o orientador sem diferenciar maiúsculas e minúsculas no e-mail")
        void registrarParecer_EmailComCaixaDiferente_DeveAceitar() {
            comEntregaParaParecer(StatusEntrega.SUBMETIDO);
            comSalvamentoDeParecer();

            EntregaResponseDTO resposta = service.registrarParecer("ORIENTADOR@IFSC.EDU.BR", 100L,
                    new ParecerRequestDTO(StatusEntrega.APROVADO, null));

            assertThat(resposta.status()).isEqualTo(StatusEntrega.APROVADO);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Deve aceitar aprovação sem comentário, gravando o comentário como nulo")
        void registrarParecer_AprovadoSemComentario_DeveGravarComentarioNulo(String comentario) {
            comEntregaParaParecer(StatusEntrega.SUBMETIDO);
            comSalvamentoDeParecer();

            EntregaResponseDTO resposta = service.registrarParecer(EMAIL_ORIENTADOR, 100L,
                    new ParecerRequestDTO(StatusEntrega.APROVADO, comentario));

            assertThat(resposta.status()).isEqualTo(StatusEntrega.APROVADO);
            assertThat(resposta.parecer().comentario()).isNull();
        }

        @Test
        @DisplayName("Deve lançar não encontrado quando a entrega não existe")
        void registrarParecer_EntregaInexistente_DeveLancarNaoEncontrado() {
            when(entregaRepository.findByIdParaAtualizacao(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.registrarParecer(EMAIL_ORIENTADOR, 99L,
                            new ParecerRequestDTO(StatusEntrega.APROVADO, null)));

            assertThat(ex.getMessage()).isEqualTo("Entrega com identificador 99 não foi encontrada.");
            verificarQueNadaFoiGravado();
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "outro.professor@ifsc.edu.br", EMAIL_ALUNO})
        @DisplayName("Deve negar quem não é o orientador vinculado à entrega")
        void registrarParecer_SemVinculo_DeveLancarAcessoNegado(String email) {
            Entrega entrega = comEntregaParaParecer(StatusEntrega.SUBMETIDO);

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> service.registrarParecer(email, 100L,
                            new ParecerRequestDTO(StatusEntrega.APROVADO, null)));

            assertThat(ex.getMessage()).isEqualTo("Somente o orientador vinculado à entrega pode registrar parecer.");
            assertThat(entrega.getStatus()).isEqualTo(StatusEntrega.SUBMETIDO);
            verificarQueNadaFoiGravado();
        }

        @ParameterizedTest
        @EnumSource(value = StatusEntrega.class, names = {"APROVADO", "REPROVADO"})
        @DisplayName("Deve bloquear novo parecer em entrega com decisão definitiva")
        void registrarParecer_ComParecerDefinitivo_DeveLancarConflito(StatusEntrega status) {
            Entrega entrega = comEntregaParaParecer(status);

            ConflitoDeEstadoException ex = assertThrows(ConflitoDeEstadoException.class,
                    () -> service.registrarParecer(EMAIL_ORIENTADOR, 100L,
                            new ParecerRequestDTO(StatusEntrega.AJUSTES_NECESSARIOS, COMENTARIO)));

            assertThat(ex.getMessage()).isEqualTo("Esta entrega já possui parecer definitivo (" + status
                    + ") e não pode ser reavaliada.");
            assertThat(entrega.getStatus()).isEqualTo(status);
            verificarQueNadaFoiGravado();
        }

        @Test
        @DisplayName("Deve bloquear novo parecer enquanto a entrega aguarda reenvio do aluno")
        void registrarParecer_AguardandoReenvio_DeveLancarConflito() {
            comEntregaParaParecer(StatusEntrega.AJUSTES_NECESSARIOS);

            ConflitoDeEstadoException ex = assertThrows(ConflitoDeEstadoException.class,
                    () -> service.registrarParecer(EMAIL_ORIENTADOR, 100L,
                            new ParecerRequestDTO(StatusEntrega.APROVADO, null)));

            assertThat(ex.getMessage()).isEqualTo(
                    "Esta entrega aguarda reenvio do aluno antes de receber um novo parecer.");
            verificarQueNadaFoiGravado();
        }

        @Test
        @DisplayName("Deve responder conflito antes de validar o conteúdo quando a entrega já foi encerrada")
        void registrarParecer_EncerradaEConteudoInvalido_DevePriorizarConflito() {
            comEntregaParaParecer(StatusEntrega.APROVADO);

            assertThrows(ConflitoDeEstadoException.class,
                    () -> service.registrarParecer(EMAIL_ORIENTADOR, 100L,
                            new ParecerRequestDTO(StatusEntrega.REPROVADO, null)));
        }

        @Test
        @DisplayName("Deve rejeitar SUBMETIDO como resultado do parecer")
        void registrarParecer_ResultadoSubmetido_DeveLancarRegraDeNegocio() {
            comEntregaParaParecer(StatusEntrega.SUBMETIDO);

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.registrarParecer(EMAIL_ORIENTADOR, 100L,
                            new ParecerRequestDTO(StatusEntrega.SUBMETIDO, COMENTARIO)));

            assertThat(ex.getMessage()).isEqualTo(
                    "O resultado do parecer deve ser APROVADO, AJUSTES_NECESSARIOS ou REPROVADO.");
            verificarQueNadaFoiGravado();
        }

        @ParameterizedTest
        @CsvSource(value = {
                "AJUSTES_NECESSARIOS,NULL", "AJUSTES_NECESSARIOS,''", "AJUSTES_NECESSARIOS,'   '",
                "REPROVADO,NULL", "REPROVADO,''", "REPROVADO,'   '"
        }, nullValues = "NULL")
        @DisplayName("Deve exigir comentário em ajustes e reprovação")
        void registrarParecer_SemComentarioObrigatorio_DeveLancarRegraDeNegocio(StatusEntrega resultado,
                                                                                String comentario) {
            Entrega entrega = comEntregaParaParecer(StatusEntrega.SUBMETIDO);

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.registrarParecer(EMAIL_ORIENTADOR, 100L,
                            new ParecerRequestDTO(resultado, comentario)));

            assertThat(ex.getMessage()).isEqualTo("O comentário com a justificativa é obrigatório "
                    + "quando o parecer exige ajustes ou reprova a entrega.");
            assertThat(entrega.getStatus()).isEqualTo(StatusEntrega.SUBMETIDO);
            verificarQueNadaFoiGravado();
        }
    }

    @Nested
    @DisplayName("Parecer nas respostas")
    class ParecerNasRespostas {

        private Parecer parecerSalvo(StatusEntrega resultado) {
            return Parecer.builder()
                    .id(50L)
                    .resultado(resultado)
                    .comentario(COMENTARIO)
                    .registradoEm(AGORA)
                    .build();
        }

        @Test
        @DisplayName("A consulta deve expor o parecer mais recente da entrega")
        void buscarPorId_ComParecer_DeveRetornarParecerMaisRecente() {
            when(entregaRepository.findById(100L))
                    .thenReturn(Optional.of(entregaSalva(TipoEntrega.PROJETO_TCC, StatusEntrega.APROVADO)));
            when(parecerRepository.findFirstByEntregaIdOrderByRegistradoEmDescIdDesc(100L))
                    .thenReturn(Optional.of(parecerSalvo(StatusEntrega.APROVADO)));

            EntregaResponseDTO resposta = service.buscarPorId(EMAIL_ALUNO, 100L);

            assertThat(resposta.parecer().id()).isEqualTo(50L);
            assertThat(resposta.parecer().resultado()).isEqualTo(StatusEntrega.APROVADO);
            assertThat(resposta.parecer().comentario()).isEqualTo(COMENTARIO);
            assertThat(resposta.parecer().registradoEm()).isEqualTo(AGORA);
        }

        @Test
        @DisplayName("O reenvio deve voltar para SUBMETIDO mantendo o último parecer registrado")
        void submeter_Reenvio_DeveManterUltimoParecer() {
            comVinculoAceito();
            comPrazo(HOJE.plusDays(10));
            Entrega existente = comEntregaExistente(StatusEntrega.AJUSTES_NECESSARIOS);
            when(entregaRepository.save(existente)).thenReturn(existente);
            when(parecerRepository.findFirstByEntregaIdOrderByRegistradoEmDescIdDesc(100L))
                    .thenReturn(Optional.of(parecerSalvo(StatusEntrega.AJUSTES_NECESSARIOS)));

            SubmissaoEntregaResultado resultado = service.submeter(EMAIL_ALUNO,
                    new EntregaRequestDTO(TipoEntrega.PROJETO_TCC, NOVA_URL));

            assertThat(resultado.entrega().status()).isEqualTo(StatusEntrega.SUBMETIDO);
            assertThat(resultado.entrega().parecer().resultado()).isEqualTo(StatusEntrega.AJUSTES_NECESSARIOS);
        }
    }
}
