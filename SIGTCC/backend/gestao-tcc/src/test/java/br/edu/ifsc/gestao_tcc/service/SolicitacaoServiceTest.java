package br.edu.ifsc.gestao_tcc.service;

import br.edu.ifsc.gestao_tcc.dto.AtualizaStatusRequest;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoRequestDTO;
import br.edu.ifsc.gestao_tcc.dto.SolicitacaoResponseDTO;
import br.edu.ifsc.gestao_tcc.event.SolicitacaoStatusChangedEvent;
import br.edu.ifsc.gestao_tcc.exception.*;
import br.edu.ifsc.gestao_tcc.model.*;
import br.edu.ifsc.gestao_tcc.repository.AlunoRepository;
import br.edu.ifsc.gestao_tcc.repository.OrientadorRepository;
import br.edu.ifsc.gestao_tcc.repository.SolicitacaoRepository;
import br.edu.ifsc.gestao_tcc.validation.ValidacaoSolicitacaoOrientacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;
    @Mock
    private OrientadorRepository orientadorRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ValidacaoSolicitacaoOrientacao validacaoMock;

    private SolicitacaoService solicitacaoService;

    @BeforeEach
    void setUp() {
        solicitacaoService = new SolicitacaoService(
                solicitacaoRepository,
                orientadorRepository,
                alunoRepository,
                eventPublisher,
                List.of(validacaoMock)
        );
    }

    @Test
    @DisplayName("Deve criar solicitação com sucesso reutilizando um aluno existente")
    void criarSolicitacao_ComAlunoExistente_DeveRetornarSucesso() {
        SolicitacaoRequestDTO requestDTO = criarRequestDTO();
        Orientador orientador = criarOrientadorMock(1L, 2);
        Aluno alunoExistente = criarAlunoMock(10L, requestDTO.getAluno().getEmail());

        when(orientadorRepository.findById(1L)).thenReturn(Optional.of(orientador));
        when(alunoRepository.findByEmail(alunoExistente.getEmail())).thenReturn(Optional.of(alunoExistente));

        when(solicitacaoRepository.save(any(SolicitacaoOrientacao.class))).thenAnswer(invocation -> {
            SolicitacaoOrientacao s = invocation.getArgument(0);
            s.setId(100L);
            s.setCriadoEm(Instant.now());
            return s;
        });

        SolicitacaoResponseDTO response = solicitacaoService.criarSolicitacao(requestDTO);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(StatusSolicitacao.PENDENTE, response.status());

        verify(validacaoMock, times(1)).validar(requestDTO, alunoExistente, orientador);
        verify(alunoRepository, never()).save(any(Aluno.class));
    }

    @Test
    @DisplayName("Deve criar solicitação com sucesso e criar um novo aluno caso não exista")
    void criarSolicitacao_ComNovoAluno_DeveRetornarSucesso() {
        SolicitacaoRequestDTO requestDTO = criarRequestDTO();
        Orientador orientador = criarOrientadorMock(1L, 2);

        when(orientadorRepository.findById(1L)).thenReturn(Optional.of(orientador));
        when(alunoRepository.findByEmail(requestDTO.getAluno().getEmail())).thenReturn(Optional.empty());

        when(alunoRepository.save(any(Aluno.class))).thenAnswer(i -> {
            Aluno a = i.getArgument(0);
            a.setId(20L);
            return a;
        });

        when(solicitacaoRepository.save(any(SolicitacaoOrientacao.class))).thenAnswer(invocation -> {
            SolicitacaoOrientacao s = invocation.getArgument(0);
            s.setId(101L);
            return s;
        });

        SolicitacaoResponseDTO response = solicitacaoService.criarSolicitacao(requestDTO);

        assertNotNull(response);
        verify(alunoRepository, times(1)).save(any(Aluno.class)); // Garante que o novo aluno foi salvo
    }

    @Test
    @DisplayName("Deve lançar exceção se o orientador não for encontrado na criação")
    void criarSolicitacao_OrientadorInexistente_DeveLancarExcecao() {
        SolicitacaoRequestDTO requestDTO = criarRequestDTO();
        when(orientadorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> solicitacaoService.criarSolicitacao(requestDTO));
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar solicitações de um orientador sem filtro de estado")
    void listarPorOrientador_SemStatus_DeveRetornarLista() {
        Orientador orientador = criarOrientadorMock(1L, 2);
        SolicitacaoOrientacao solicitacao = criarSolicitacaoMock(100L, StatusSolicitacao.PENDENTE, orientador);

        when(orientadorRepository.findById(1L)).thenReturn(Optional.of(orientador));
        when(solicitacaoRepository.findByOrientadorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(solicitacao));

        List<SolicitacaoResponseDTO> resultado = solicitacaoService.listarPorOrientador(1L, null);

        assertEquals(1, resultado.size());
        assertEquals(StatusSolicitacao.PENDENTE, resultado.get(0).status());
    }

    @Test
    @DisplayName("Deve listar solicitações de um orientador com filtro de estado válido")
    void listarPorOrientador_ComStatus_DeveRetornarListaFiltrada() {
        Orientador orientador = criarOrientadorMock(1L, 2);
        SolicitacaoOrientacao solicitacao = criarSolicitacaoMock(100L, StatusSolicitacao.ACEITA, orientador);

        when(orientadorRepository.findById(1L)).thenReturn(Optional.of(orientador));
        when(solicitacaoRepository.findByOrientadorIdAndStatusOrderByCriadoEmDesc(1L, StatusSolicitacao.ACEITA))
                .thenReturn(List.of(solicitacao));

        List<SolicitacaoResponseDTO> resultado = solicitacaoService.listarPorOrientador(1L, "ACEITA");

        assertEquals(1, resultado.size());
        assertEquals(StatusSolicitacao.ACEITA, resultado.get(0).status());
    }

    @Test
    @DisplayName("Deve atualizar o estado para ACEITA e publicar evento")
    void atualizarStatus_ParaAceita_DeveAtualizarEPublicarEvento() {
        Orientador orientador = criarOrientadorMock(1L, 1);
        SolicitacaoOrientacao solicitacao = criarSolicitacaoMock(100L, StatusSolicitacao.PENDENTE, orientador);
        AtualizaStatusRequest request = new AtualizaStatusRequest(StatusSolicitacao.ACEITA, null);

        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));

        SolicitacaoResponseDTO response = solicitacaoService.atualizarStatus(100L, request);

        assertEquals(StatusSolicitacao.ACEITA, response.status());
        assertNull(response.justificativa());

        ArgumentCaptor<SolicitacaoStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(SolicitacaoStatusChangedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(StatusSolicitacao.ACEITA, eventCaptor.getValue().statusNovo());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar aceitar solicitação sem vagas disponíveis")
    void atualizarStatus_ParaAceitaSemVagas_DeveLancarExcecao() {
        Orientador orientador = criarOrientadorMock(1L, 0); // Zero vagas
        SolicitacaoOrientacao solicitacao = criarSolicitacaoMock(100L, StatusSolicitacao.PENDENTE, orientador);
        AtualizaStatusRequest request = new AtualizaStatusRequest(StatusSolicitacao.ACEITA, null);

        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));

        assertThrows(VagasIndisponiveisException.class, () -> solicitacaoService.atualizarStatus(100L, request));
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar o estado para RECUSADA com justificativa válida")
    void atualizarStatus_ParaRecusada_DeveAtualizar() {
        Orientador orientador = criarOrientadorMock(1L, 2);
        SolicitacaoOrientacao solicitacao = criarSolicitacaoMock(100L, StatusSolicitacao.PENDENTE, orientador);
        AtualizaStatusRequest request = new AtualizaStatusRequest(StatusSolicitacao.RECUSADA, "Não tenho disponibilidade na agenda.");

        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));

        SolicitacaoResponseDTO response = solicitacaoService.atualizarStatus(100L, request);

        assertEquals(StatusSolicitacao.RECUSADA, response.status());
        assertEquals("Não tenho disponibilidade na agenda.", response.justificativa());
    }

    @Test
    @DisplayName("Deve lançar erro ao recusar sem fornecer justificativa")
    void atualizarStatus_ParaRecusadaSemJustificativa_DeveLancarExcecao() {
        Orientador orientador = criarOrientadorMock(1L, 2);
        SolicitacaoOrientacao solicitacao = criarSolicitacaoMock(100L, StatusSolicitacao.PENDENTE, orientador);
        AtualizaStatusRequest request = new AtualizaStatusRequest(StatusSolicitacao.RECUSADA, null);

        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));

        assertThrows(ValidacaoRequisicaoException.class, () -> solicitacaoService.atualizarStatus(100L, request));
    }

    @Test
    @DisplayName("Deve lançar erro de conflito ao tentar atualizar solicitação já respondida")
    void atualizarStatus_SolicitacaoJaRespondida_DeveLancarExcecao() {
        Orientador orientador = criarOrientadorMock(1L, 2);
        // Solicitação já com estado ACEITA
        SolicitacaoOrientacao solicitacao = criarSolicitacaoMock(100L, StatusSolicitacao.ACEITA, orientador);
        AtualizaStatusRequest request = new AtualizaStatusRequest(StatusSolicitacao.RECUSADA, "Justificativa teste");

        when(solicitacaoRepository.findById(100L)).thenReturn(Optional.of(solicitacao));

        assertThrows(SolicitacaoJaRespondidaException.class, () -> solicitacaoService.atualizarStatus(100L, request));
    }


    private SolicitacaoRequestDTO criarRequestDTO() {
        SolicitacaoRequestDTO dto = new SolicitacaoRequestDTO();
        dto.setOrientadorId(1L);
        dto.setTema("IA na Saúde");
        dto.setMensagem("Gostaria de ser orientado.");

        SolicitacaoRequestDTO.AlunoDTO alunoDTO = new SolicitacaoRequestDTO.AlunoDTO();
        alunoDTO.setNome("João");
        alunoDTO.setEmail("joao@ifsc.edu.br");
        alunoDTO.setCurso("Engenharia");
        dto.setAluno(alunoDTO);

        return dto;
    }

    private Orientador criarOrientadorMock(Long id, int vagas) {
        PerfilOrientador perfil = PerfilOrientador.builder().vagasDisponiveis(vagas).build();
        Orientador orientador = new Orientador();
        orientador.setId(id);
        orientador.setNome("Dr. Orientador");
        orientador.setEmail("orientador@ifsc.edu.br");
        orientador.setPerfil(perfil);
        return orientador;
    }

    private Aluno criarAlunoMock(Long id, String email) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome("João");
        aluno.setEmail(email);
        aluno.setCurso("Engenharia");
        return aluno;
    }

    private SolicitacaoOrientacao criarSolicitacaoMock(Long id, StatusSolicitacao status, Orientador orientador) {
        return SolicitacaoOrientacao.builder()
                .id(id)
                .orientador(orientador)
                .aluno(criarAlunoMock(10L, "aluno@ifsc.edu.br"))
                .tema("Tema teste")
                .mensagem("Mensagem teste")
                .status(status)
                .criadoEm(Instant.now())
                .build();
    }
}