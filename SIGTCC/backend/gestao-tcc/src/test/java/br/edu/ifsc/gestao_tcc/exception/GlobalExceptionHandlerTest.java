package br.edu.ifsc.gestao_tcc.exception;

import br.edu.ifsc.gestao_tcc.dto.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/orientadores");
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

    /**
     * Enum mínimo usado só neste teste para simular {@code StatusSolicitacao}
     * sem depender da entidade real da US04 (ainda não implementada nesta
     * issue) — evita acoplar o teste do handler ao modelo de domínio.
     */
    private enum StatusSolicitacaoTeste {
        PENDENTE, ACEITA, RECUSADA, CANCELADA
    }
}
