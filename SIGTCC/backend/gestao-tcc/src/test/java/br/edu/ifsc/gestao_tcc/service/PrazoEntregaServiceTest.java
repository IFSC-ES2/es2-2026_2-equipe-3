package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaResponseDTO;
import br.edu.ifsc.gestao_tcc.dto.PrazoEntregaUpdateDTO;
import br.edu.ifsc.gestao_tcc.exception.AcessoNegadoException;
import br.edu.ifsc.gestao_tcc.exception.PrazoJaCadastradoException;
import br.edu.ifsc.gestao_tcc.exception.RegraDeNegocioException;
import br.edu.ifsc.gestao_tcc.exception.ResourceNotFoundException;
import br.edu.ifsc.gestao_tcc.exception.ValidacaoRequisicaoException;
import br.edu.ifsc.gestao_tcc.model.Orientador;
import br.edu.ifsc.gestao_tcc.model.PrazoEntrega;
import br.edu.ifsc.gestao_tcc.model.TipoEntrega;
import br.edu.ifsc.gestao_tcc.model.TurmaTCC;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.PrazoEntregaRepository;
import br.edu.ifsc.gestao_tcc.repository.TurmaTCCRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrazoEntregaServiceTest {

    /** "Hoje" fixo nos testes: 15/10/2026 no fuso de São Paulo. */
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 15);
    private static final Clock RELOGIO_FIXO = Clock.fixed(
            Instant.parse("2026-10-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private static final String EMAIL_RESPONSAVEL = "responsavel@ifsc.edu.br";
    private static final String EMAIL_ORIENTADOR = "orientador@ifsc.edu.br";

    @Mock
    private PrazoEntregaRepository prazoEntregaRepository;

    @Mock
    private TurmaTCCRepository turmaTCCRepository;

    @Mock
    private OrientadorRepository orientadorRepository;

    private PrazoEntregaService service;

    private Orientador responsavel;
    private Orientador orientador;
    private TurmaTCC turmaVigente;

    @BeforeEach
    void setUp() {
        service = new PrazoEntregaService(prazoEntregaRepository, turmaTCCRepository, orientadorRepository, RELOGIO_FIXO);

        responsavel = professor(1L, EMAIL_RESPONSAVEL);
        orientador = professor(2L, EMAIL_ORIENTADOR);
        turmaVigente = turma(10L, "2026/2", true, responsavel);
    }

    private Orientador professor(Long id, String email) {
        Orientador professor = new Orientador();
        professor.setId(id);
        professor.setNome("Professor " + id);
        professor.setEmail(email);
        return professor;
    }

    private TurmaTCC turma(Long id, String semestre, boolean vigente, Orientador responsavel) {
        return TurmaTCC.builder()
                .id(id)
                .semestre(semestre)
                .vigente(vigente)
                .professorResponsavel(responsavel)
                .build();
    }

    private PrazoEntrega prazo(Long id, TurmaTCC turma, TipoEntrega tipo, LocalDate dataLimite) {
        return PrazoEntrega.builder()
                .id(id)
                .turma(turma)
                .tipo(tipo)
                .dataLimite(dataLimite)
                .build();
    }

    private void autenticadoComoResponsavel() {
        when(orientadorRepository.findByEmailIgnoreCase(EMAIL_RESPONSAVEL)).thenReturn(Optional.of(responsavel));
        when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.of(turmaVigente));
    }

    private void saveDevolveOProprioPrazo() {
        lenient().when(prazoEntregaRepository.save(any(PrazoEntrega.class))).thenAnswer(invocacao -> {
            PrazoEntrega salvo = invocacao.getArgument(0);
            if (salvo.getId() == null) {
                salvo.setId(100L);
            }
            return salvo;
        });
    }

    @Nested
    @DisplayName("Autorização")
    class Autorizacao {

        @Test
        @DisplayName("Deve negar o cadastro quando o professor não é o responsável pela turma vigente")
        void cadastrar_ProfessorNaoResponsavel_DeveLancarAcessoNegado() {
            when(orientadorRepository.findByEmailIgnoreCase(EMAIL_ORIENTADOR)).thenReturn(Optional.of(orientador));
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.of(turmaVigente));

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25));

            assertThrows(AcessoNegadoException.class, () -> service.cadastrar(EMAIL_ORIENTADOR, dto));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve negar o cadastro quando o e-mail do token não pertence a nenhum professor")
        void cadastrar_ProfessorNaoCadastrado_DeveLancarAcessoNegado() {
            when(orientadorRepository.findByEmailIgnoreCase("desconhecido@ifsc.edu.br")).thenReturn(Optional.empty());

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25));

            assertThrows(AcessoNegadoException.class, () -> service.cadastrar("desconhecido@ifsc.edu.br", dto));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve negar o cadastro quando o token não traz e-mail")
        void cadastrar_SemEmail_DeveLancarAcessoNegado() {
            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25));

            assertThrows(AcessoNegadoException.class, () -> service.cadastrar(null, dto));
            assertThrows(AcessoNegadoException.class, () -> service.cadastrar("  ", dto));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve negar o cadastro quando não existe turma vigente")
        void cadastrar_SemTurmaVigente_DeveLancarAcessoNegado() {
            when(orientadorRepository.findByEmailIgnoreCase(EMAIL_RESPONSAVEL)).thenReturn(Optional.of(responsavel));
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.empty());

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25));

            assertThrows(AcessoNegadoException.class, () -> service.cadastrar(EMAIL_RESPONSAVEL, dto));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve negar a edição ao professor não responsável sem sequer consultar o prazo")
        void atualizar_ProfessorNaoResponsavel_DeveLancarAcessoNegado() {
            when(orientadorRepository.findByEmailIgnoreCase(EMAIL_ORIENTADOR)).thenReturn(Optional.of(orientador));
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.of(turmaVigente));

            var dto = new PrazoEntregaUpdateDTO(LocalDate.of(2026, 12, 1));

            assertThrows(AcessoNegadoException.class, () -> service.atualizar(EMAIL_ORIENTADOR, 5L, dto));
            verify(prazoEntregaRepository, never()).findById(anyLong());
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve aceitar o e-mail do responsável independentemente de maiúsculas e minúsculas")
        void cadastrar_EmailComCaixaDiferente_DevePermitir() {
            when(orientadorRepository.findByEmailIgnoreCase("Responsavel@IFSC.edu.br")).thenReturn(Optional.of(responsavel));
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.of(turmaVigente));
            saveDevolveOProprioPrazo();

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25));

            PrazoEntregaResponseDTO resposta = service.cadastrar("Responsavel@IFSC.edu.br", dto);

            assertThat(resposta.tipo()).isEqualTo(TipoEntrega.PROJETO_TCC);
        }
    }

    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("Deve cadastrar o prazo vinculado à turma vigente")
        void cadastrar_DadosValidos_DeveSalvarPrazo() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.existsByTurmaIdAndTipo(10L, TipoEntrega.PROJETO_TCC)).thenReturn(false);
            saveDevolveOProprioPrazo();

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25));

            PrazoEntregaResponseDTO resposta = service.cadastrar(EMAIL_RESPONSAVEL, dto);

            ArgumentCaptor<PrazoEntrega> captor = ArgumentCaptor.forClass(PrazoEntrega.class);
            verify(prazoEntregaRepository).save(captor.capture());
            assertThat(captor.getValue().getTurma()).isSameAs(turmaVigente);
            assertThat(captor.getValue().getTipo()).isEqualTo(TipoEntrega.PROJETO_TCC);
            assertThat(captor.getValue().getDataLimite()).isEqualTo(LocalDate.of(2026, 11, 25));

            assertThat(resposta.id()).isEqualTo(100L);
            assertThat(resposta.semestre()).isEqualTo("2026/2");
            assertThat(resposta.descricao()).isEqualTo("Projeto de TCC");
            assertThat(resposta.dataLimite()).isEqualTo(LocalDate.of(2026, 11, 25));
        }

        @Test
        @DisplayName("Deve cadastrar os três tipos de entrega em sequência")
        void cadastrar_TresTiposEmSequencia_DeveSalvarTodos() {
            autenticadoComoResponsavel();
            saveDevolveOProprioPrazo();

            service.cadastrar(EMAIL_RESPONSAVEL, new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25)));
            service.cadastrar(EMAIL_RESPONSAVEL, new PrazoEntregaRequestDTO(TipoEntrega.TCC1, LocalDate.of(2027, 3, 15)));
            PrazoEntregaResponseDTO tcc2 = service.cadastrar(EMAIL_RESPONSAVEL,
                    new PrazoEntregaRequestDTO(TipoEntrega.TCC2, LocalDate.of(2027, 7, 10)));

            verify(prazoEntregaRepository, times(3)).save(any(PrazoEntrega.class));
            assertThat(tcc2.tipo()).isEqualTo(TipoEntrega.TCC2);
        }

        @Test
        @DisplayName("Deve rejeitar o cadastro de um tipo que já tem prazo na turma vigente")
        void cadastrar_TipoJaCadastrado_DeveLancarConflito() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.existsByTurmaIdAndTipo(10L, TipoEntrega.TCC1)).thenReturn(true);

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.TCC1, LocalDate.of(2027, 3, 15));

            PrazoJaCadastradoException ex = assertThrows(PrazoJaCadastradoException.class,
                    () -> service.cadastrar(EMAIL_RESPONSAVEL, dto));
            assertThat(ex.getMessage()).contains("TCC 1").contains("2026/2");
            verify(prazoEntregaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Edição")
    class Edicao {

        @Test
        @DisplayName("Deve substituir a data limite de um prazo já cadastrado")
        void atualizar_DadosValidos_DeveSubstituirData() {
            autenticadoComoResponsavel();
            saveDevolveOProprioPrazo();
            PrazoEntrega tcc1 = prazo(2L, turmaVigente, TipoEntrega.TCC1, LocalDate.of(2027, 3, 15));
            when(prazoEntregaRepository.findById(2L)).thenReturn(Optional.of(tcc1));

            PrazoEntregaResponseDTO resposta = service.atualizar(EMAIL_RESPONSAVEL, 2L,
                    new PrazoEntregaUpdateDTO(LocalDate.of(2027, 4, 1)));

            assertThat(tcc1.getDataLimite()).isEqualTo(LocalDate.of(2027, 4, 1));
            assertThat(resposta.id()).isEqualTo(2L);
            assertThat(resposta.tipo()).isEqualTo(TipoEntrega.TCC1);
            assertThat(resposta.dataLimite()).isEqualTo(LocalDate.of(2027, 4, 1));
            verify(prazoEntregaRepository).save(tcc1);
        }

        @Test
        @DisplayName("Deve lançar 404 quando o prazo não existe")
        void atualizar_PrazoInexistente_DeveLancarNaoEncontrado() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.findById(99L)).thenReturn(Optional.empty());

            var dto = new PrazoEntregaUpdateDTO(LocalDate.of(2027, 4, 1));

            assertThrows(ResourceNotFoundException.class, () -> service.atualizar(EMAIL_RESPONSAVEL, 99L, dto));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar a edição de prazo de uma turma que não é a vigente")
        void atualizar_PrazoDeTurmaAntiga_DeveLancarRegraDeNegocio() {
            autenticadoComoResponsavel();
            TurmaTCC turmaAntiga = turma(9L, "2026/1", false, responsavel);
            when(prazoEntregaRepository.findById(7L)).thenReturn(Optional.of(
                    prazo(7L, turmaAntiga, TipoEntrega.TCC1, LocalDate.of(2026, 6, 30))));

            var dto = new PrazoEntregaUpdateDTO(LocalDate.of(2027, 4, 1));

            assertThrows(RegraDeNegocioException.class, () -> service.atualizar(EMAIL_RESPONSAVEL, 7L, dto));
            verify(prazoEntregaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta")
    class Consulta {

        @Test
        @DisplayName("Deve listar os prazos da turma vigente na ordem do fluxo")
        void listarPrazosVigentes_DeveOrdenarPorTipo() {
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.of(turmaVigente));
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(
                    prazo(3L, turmaVigente, TipoEntrega.TCC2, LocalDate.of(2027, 7, 10)),
                    prazo(1L, turmaVigente, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25)),
                    prazo(2L, turmaVigente, TipoEntrega.TCC1, LocalDate.of(2027, 3, 15))));

            List<PrazoEntregaResponseDTO> prazos = service.listarPrazosVigentes();

            assertThat(prazos).extracting(PrazoEntregaResponseDTO::tipo)
                    .containsExactly(TipoEntrega.PROJETO_TCC, TipoEntrega.TCC1, TipoEntrega.TCC2);
            assertThat(prazos).allSatisfy(p -> assertThat(p.semestre()).isEqualTo("2026/2"));
        }

        @Test
        @DisplayName("Deve devolver lista vazia quando não existe turma vigente")
        void listarPrazosVigentes_SemTurmaVigente_DeveRetornarListaVazia() {
            when(turmaTCCRepository.findByVigenteTrue()).thenReturn(Optional.empty());

            assertThat(service.listarPrazosVigentes()).isEmpty();
            verify(prazoEntregaRepository, never()).findByTurmaId(anyLong());
        }
    }

    @Nested
    @DisplayName("Validação de datas (#138)")
    class ValidacaoDeDatas {

        @Test
        @DisplayName("Deve rejeitar o cadastro com data limite no passado")
        void cadastrar_DataNoPassado_DeveLancarValidacao() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of());

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, HOJE.minusDays(1));

            ValidacaoRequisicaoException ex = assertThrows(ValidacaoRequisicaoException.class,
                    () -> service.cadastrar(EMAIL_RESPONSAVEL, dto));
            assertThat(ex.getCampo()).isEqualTo("dataLimite");
            assertThat(ex.getMessage()).isEqualTo("A data limite não pode estar no passado.");
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve aceitar data limite igual a hoje")
        void cadastrar_DataDeHoje_DevePermitir() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of());
            saveDevolveOProprioPrazo();

            PrazoEntregaResponseDTO resposta = service.cadastrar(EMAIL_RESPONSAVEL,
                    new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, HOJE));

            assertThat(resposta.dataLimite()).isEqualTo(HOJE);
        }

        @Test
        @DisplayName("Deve rejeitar TCC 1 com data anterior ou igual ao prazo do Projeto de TCC")
        void cadastrar_Tcc1AntesDoProjeto_DeveLancarRegraDeNegocio() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(
                    prazo(1L, turmaVigente, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25))));

            var anterior = new PrazoEntregaRequestDTO(TipoEntrega.TCC1, LocalDate.of(2026, 11, 20));
            var igual = new PrazoEntregaRequestDTO(TipoEntrega.TCC1, LocalDate.of(2026, 11, 25));

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.cadastrar(EMAIL_RESPONSAVEL, anterior));
            assertThat(ex.getMessage()).isEqualTo(
                    "A data limite de TCC 1 (20/11/2026) deve ser posterior à data limite de Projeto de TCC (25/11/2026).");
            assertThrows(RegraDeNegocioException.class, () -> service.cadastrar(EMAIL_RESPONSAVEL, igual));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar TCC 2 com data anterior ao prazo do TCC 1")
        void cadastrar_Tcc2AntesDoTcc1_DeveLancarRegraDeNegocio() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(
                    prazo(1L, turmaVigente, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25)),
                    prazo(2L, turmaVigente, TipoEntrega.TCC1, LocalDate.of(2027, 3, 15))));

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.TCC2, LocalDate.of(2027, 3, 1));

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.cadastrar(EMAIL_RESPONSAVEL, dto));
            assertThat(ex.getMessage()).isEqualTo(
                    "A data limite de TCC 2 (01/03/2027) deve ser posterior à data limite de TCC 1 (15/03/2027).");
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar Projeto de TCC com data posterior ao prazo já definido para TCC 1")
        void cadastrar_ProjetoDepoisDoTcc1_DeveLancarRegraDeNegocio() {
            autenticadoComoResponsavel();
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(
                    prazo(2L, turmaVigente, TipoEntrega.TCC1, LocalDate.of(2027, 3, 15))));

            var dto = new PrazoEntregaRequestDTO(TipoEntrega.PROJETO_TCC, LocalDate.of(2027, 4, 1));

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.cadastrar(EMAIL_RESPONSAVEL, dto));
            assertThat(ex.getMessage()).isEqualTo(
                    "A data limite de Projeto de TCC (01/04/2027) deve ser anterior à data limite de TCC 1 (15/03/2027).");
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve aceitar a sequência Projeto de TCC, TCC 1 e TCC 2 em ordem")
        void cadastrar_SequenciaValida_DevePermitir() {
            autenticadoComoResponsavel();
            saveDevolveOProprioPrazo();
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(
                    prazo(1L, turmaVigente, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25)),
                    prazo(3L, turmaVigente, TipoEntrega.TCC2, LocalDate.of(2027, 7, 10))));

            PrazoEntregaResponseDTO resposta = service.cadastrar(EMAIL_RESPONSAVEL,
                    new PrazoEntregaRequestDTO(TipoEntrega.TCC1, LocalDate.of(2027, 3, 15)));

            assertThat(resposta.tipo()).isEqualTo(TipoEntrega.TCC1);
            verify(prazoEntregaRepository).save(any(PrazoEntrega.class));
        }

        @Test
        @DisplayName("Deve rejeitar a edição para uma data no passado")
        void atualizar_DataNoPassado_DeveLancarValidacao() {
            autenticadoComoResponsavel();
            PrazoEntrega projeto = prazo(1L, turmaVigente, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25));
            when(prazoEntregaRepository.findById(1L)).thenReturn(Optional.of(projeto));
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(projeto));

            var dto = new PrazoEntregaUpdateDTO(HOJE.minusDays(3));

            assertThrows(ValidacaoRequisicaoException.class, () -> service.atualizar(EMAIL_RESPONSAVEL, 1L, dto));
            assertThat(projeto.getDataLimite()).isEqualTo(LocalDate.of(2026, 11, 25));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar a edição que coloca TCC 1 depois do prazo de TCC 2")
        void atualizar_ViolandoSequencia_DeveLancarRegraDeNegocio() {
            autenticadoComoResponsavel();
            PrazoEntrega tcc1 = prazo(2L, turmaVigente, TipoEntrega.TCC1, LocalDate.of(2027, 3, 15));
            when(prazoEntregaRepository.findById(2L)).thenReturn(Optional.of(tcc1));
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(
                    tcc1, prazo(3L, turmaVigente, TipoEntrega.TCC2, LocalDate.of(2027, 7, 10))));

            var dto = new PrazoEntregaUpdateDTO(LocalDate.of(2027, 8, 1));

            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> service.atualizar(EMAIL_RESPONSAVEL, 2L, dto));
            assertThat(ex.getMessage()).contains("deve ser anterior à data limite de TCC 2");
            assertThat(tcc1.getDataLimite()).isEqualTo(LocalDate.of(2027, 3, 15));
            verify(prazoEntregaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve ignorar o próprio prazo ao validar a sequência na edição")
        void atualizar_ComparandoComOProprioPrazo_DevePermitir() {
            autenticadoComoResponsavel();
            saveDevolveOProprioPrazo();
            PrazoEntrega tcc1 = prazo(2L, turmaVigente, TipoEntrega.TCC1, LocalDate.of(2027, 3, 15));
            when(prazoEntregaRepository.findById(2L)).thenReturn(Optional.of(tcc1));
            when(prazoEntregaRepository.findByTurmaId(10L)).thenReturn(List.of(
                    prazo(1L, turmaVigente, TipoEntrega.PROJETO_TCC, LocalDate.of(2026, 11, 25)),
                    tcc1,
                    prazo(3L, turmaVigente, TipoEntrega.TCC2, LocalDate.of(2027, 7, 10))));

            PrazoEntregaResponseDTO resposta = service.atualizar(EMAIL_RESPONSAVEL, 2L,
                    new PrazoEntregaUpdateDTO(LocalDate.of(2027, 3, 10)));

            assertThat(resposta.dataLimite()).isEqualTo(LocalDate.of(2027, 3, 10));
        }
    }
}
