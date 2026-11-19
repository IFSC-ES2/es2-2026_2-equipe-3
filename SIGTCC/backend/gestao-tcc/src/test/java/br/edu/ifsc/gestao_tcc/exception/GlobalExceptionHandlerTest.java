package br.edu.ifsc.gestao_tcc.exception;

import br.edu.ifsc.gestao_tcc.dto.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final Instant INSTANTE_FIXO = Instant.parse("2026-11-17T10:00:00Z");
    private final Clock clockFixo = Clock.fixed(INSTANTE_FIXO, ZoneOffset.UTC);
    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(clockFixo);
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/orientadores");
    }

    @Test
    @DisplayName("Deve gerar timestamp no ErrorResponseDTO a partir do Clock injetado")
    void deveUsarClockInjetadoParaGerarTimestamp() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Orientador não encontrado");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isEqualTo(INSTANTE_FIXO.toString());
    }

    @Test
    @DisplayName("Deve capturar ResourceNotFoundException e formatar como status 404")
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Orientador não encontrado");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().erro()).isEqualTo("Recurso não encontrado");
        assertThat(response.getBody().caminho()).isEqualTo("/api/v1/orientadores");
        assertThat(response.getBody().timestamp()).isNotBlank();
        // Para 404, detalhes permanece string — não é um erro de campo.
        assertThat(response.getBody().detalhes()).isEqualTo("Orientador não encontrado");
    }

    @Test
    @DisplayName("Deve capturar AcessoNegadoException e formatar como status 403")
    void testHandleAcessoNegadoException() {
        request.setRequestURI("/api/v1/prazos");
        AcessoNegadoException ex = new AcessoNegadoException(
                "Somente o professor responsável pela Turma_TCC vigente pode gerenciar os prazos de entrega.");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleAcessoNegadoException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().erro()).isEqualTo("Acesso negado");
        assertThat(response.getBody().caminho()).isEqualTo("/api/v1/prazos");
        assertThat(response.getBody().detalhes()).isEqualTo(
                "Somente o professor responsável pela Turma_TCC vigente pode gerenciar os prazos de entrega.");
    }

    @Test
    @DisplayName("Deve capturar EmailDuplicadoException e formatar como status 409")
    void testHandleEmailDuplicadoException() {
        EmailDuplicadoException ex = new EmailDuplicadoException("E-mail já cadastrado");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleEmailDuplicadoException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().erro()).isEqualTo("Conflito de dados");
        assertThat(response.getBody().caminho()).isEqualTo("/api/v1/orientadores");
    }

    @Test
    @DisplayName("Deve capturar ConflitoDeEstadoException (SolicitacaoDuplicadaException) e formatar como status 409")
    void testHandleConflitoDeEstadoException_SolicitacaoDuplicada() {
        SolicitacaoDuplicadaException ex =
                new SolicitacaoDuplicadaException("Você já possui uma solicitação pendente para este orientador.");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleConflitoDeEstadoException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().detalhes())
                .isEqualTo("Você já possui uma solicitação pendente para este orientador.");
    }

    @Test
    @DisplayName("Deve capturar ConflitoDeEstadoException (SolicitacaoJaRespondidaException) e formatar como status 409")
    void testHandleConflitoDeEstadoException_SolicitacaoJaRespondida() {
        SolicitacaoJaRespondidaException ex =
                new SolicitacaoJaRespondidaException("Esta solicitação já foi respondida e não pode ser alterada.");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleConflitoDeEstadoException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
    }

    @Test
    @DisplayName("Deve capturar RegraDeNegocioException (VagasIndisponiveisException) e formatar como status 422")
    void testHandleRegraDeNegocioException_VagasIndisponiveis() {
        VagasIndisponiveisException ex =
                new VagasIndisponiveisException("O orientador não possui vagas disponíveis.");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleRegraDeNegocioException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT); // Atualizado
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(422);
        assertThat(response.getBody().erro()).isEqualTo("Regra de negócio violada");
        assertThat(response.getBody().detalhes()).isEqualTo("O orientador não possui vagas disponíveis.");
    }

    @Test
    @DisplayName("Deve capturar RegraDeNegocioException (OrientadorInativoException) e formatar como status 422")
    void testHandleRegraDeNegocioException_OrientadorInativo() {
        OrientadorInativoException ex =
                new OrientadorInativoException("O orientador não está disponível para receber solicitações.");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleRegraDeNegocioException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody().status()).isEqualTo(422);
    }

    @Test
    @DisplayName("Deve capturar MethodArgumentNotValidException e formatar detalhes como array de campo/mensagem")
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("orientadorRequestDTO", "email", "Formato de e-mail inválido.");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().erro()).isEqualTo("Erro de validação nos dados enviados");
        assertThat(response.getBody().caminho()).isEqualTo("/api/v1/orientadores");

        // `detalhes` deve ser um array (List), nunca um objeto/mapa.
        assertThat(response.getBody().detalhes()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<Object> detalhes = (List<Object>) response.getBody().detalhes();
        assertThat(detalhes).hasSize(1);
    }

    @Test
    @DisplayName("Duas violações no mesmo campo devem gerar duas entradas no array, sem erro 500")
    void testHandleMethodArgumentNotValidException_DuasViolacoesMesmoCampo() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError violacaoNotBlank = new FieldError("solicitacaoRequestDTO", "tema", "O tema é obrigatório.");
        FieldError violacaoSize =
                new FieldError("solicitacaoRequestDTO", "tema", "O tema deve ter entre 5 e 150 caracteres.");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(violacaoNotBlank, violacaoSize));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        List<Object> detalhes = (List<Object>) response.getBody().detalhes();
        assertThat(detalhes).hasSize(2);
    }

    @Test
    @DisplayName("Deve capturar HttpMessageNotReadableException e formatar como status 400 com detalhes string")
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponseDTO> response =
                exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().detalhes()).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("Deve capturar MethodArgumentTypeMismatchException (ex.: ?status= inválido) e formatar como status 400")
    void testHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("status");
        // Uso de Object.class simula um parâmetro sem tipo enum conhecido, cobrindo
        // o branch em que getRequiredType() não é enum ou é indisponível em runtime.
        doReturn(null).when(ex).getRequiredType();

        ResponseEntity<ErrorResponseDTO> response =
                exceptionHandler.handleMethodArgumentTypeMismatchException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().detalhes().toString()).contains("status");
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException com tipo enum deve listar os valores aceitos na mensagem")
    void testHandleMethodArgumentTypeMismatchException_ComEnum() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("status");
        doReturn(StatusSolicitacaoTeste.class).when(ex).getRequiredType();

        ResponseEntity<ErrorResponseDTO> response =
                exceptionHandler.handleMethodArgumentTypeMismatchException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().detalhes().toString()).contains("PENDENTE");
    }

    // --- Handlers genéricos (issue #262) ---

    @Test
    @DisplayName("Deve capturar DataIntegrityViolationException e formatar como 409 sem expor a constraint")
    void testHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement; constraint [uk_usuario_email]");

        ResponseEntity<ErrorResponseDTO> response =
                exceptionHandler.handleDataIntegrityViolationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().erro()).isEqualTo("Conflito de dados");
        assertThat(response.getBody().caminho()).isEqualTo("/api/v1/orientadores");
        assertThat(response.getBody().timestamp()).isNotBlank();
        assertThat(response.getBody().detalhes()).isEqualTo(
                "A operação viola uma restrição de integridade dos dados, como um valor que já está cadastrado.");
        assertThat(response.getBody().detalhes().toString()).doesNotContain("uk_usuario_email");
    }

    @Test
    @DisplayName("Deve capturar exceção inesperada e formatar como 500 sem expor a mensagem interna")
    void testHandleException_ErroInesperado() {
        RuntimeException ex = new IllegalStateException("NullPointer em OrientadorService linha 42");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().erro()).isEqualTo("Erro interno do servidor");
        assertThat(response.getBody().caminho()).isEqualTo("/api/v1/orientadores");
        assertThat(response.getBody().detalhes())
                .isEqualTo("Ocorreu um erro inesperado. Tente novamente mais tarde.");
        assertThat(response.getBody().detalhes().toString()).doesNotContain("NullPointer");
    }

    @Test
    @DisplayName("Exceção do Spring com status 404 deve manter o status no formato ErrorResponseDTO")
    void testHandleException_ErrorResponse404() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Rota inexistente.");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().erro()).isEqualTo("Recurso não encontrado");
        assertThat(response.getBody().detalhes()).isEqualTo("Rota inexistente.");
    }

    @Test
    @DisplayName("Método HTTP não suportado deve manter o 405 no formato ErrorResponseDTO")
    void testHandleException_MetodoNaoSuportado() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PUT");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(405);
        assertThat(response.getBody().erro()).isEqualTo("Requisição inválida");
        assertThat(response.getBody().detalhes().toString()).contains("PUT");
    }

    static Stream<Arguments> excecoesComHandlerEspecifico() {
        return Stream.of(
                Arguments.of(new ResourceNotFoundException("x"), "handleResourceNotFoundException"),
                Arguments.of(new EmailDuplicadoException("x"), "handleEmailDuplicadoException"),
                Arguments.of(new SolicitacaoDuplicadaException("x"), "handleConflitoDeEstadoException"),
                Arguments.of(new OptimisticLockingFailureException("x"), "handleOptimisticLockingFailureException"),
                Arguments.of(new VagasIndisponiveisException("x"), "handleRegraDeNegocioException"),
                Arguments.of(mock(MethodArgumentNotValidException.class), "handleValidationExceptions"),
                Arguments.of(mock(HttpMessageNotReadableException.class), "handleHttpMessageNotReadableException"),
                Arguments.of(mock(MethodArgumentTypeMismatchException.class),
                        "handleMethodArgumentTypeMismatchException"),
                Arguments.of(new AcessoNegadoException("x"), "handleAcessoNegadoException"),
                Arguments.of(new ValidacaoRequisicaoException("campo", "x"), "handleValidacaoRequisicaoException"),
                Arguments.of(new DataIntegrityViolationException("x"), "handleDataIntegrityViolationException"),
                Arguments.of(new IllegalStateException("x"), "handleException"),
                Arguments.of(new HttpRequestMethodNotSupportedException("PUT"), "handleException"));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("excecoesComHandlerEspecifico")
    @DisplayName("Handlers específicos devem ter precedência sobre o handler genérico de Exception")
    void testPrecedenciaDosHandlers(Exception ex, String handlerEsperado) {
        ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class);

        Method handler = resolver.resolveMethod(ex);

        assertThat(handler).isNotNull();
        assertThat(handler.getName()).isEqualTo(handlerEsperado);
    }

    /**
     * Enum mínimo usado só neste teste para simular {@code StatusSolicitacao}
     * sem depender da entidade real da US04 (ainda não implementada nesta
     * issue) — evita acoplar o teste do handler ao modelo de domínio.
     */
    private enum StatusSolicitacaoTeste {
        PENDENTE, ACEITA, RECUSADA, CANCELADA
    }
}
