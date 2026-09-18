package br.edu.ifsc.gestao_tcc.exception;

import br.edu.ifsc.gestao_tcc.dto.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("Deve capturar MethodArgumentNotValidException e formatar como status 400 com detalhes")
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

        assertThat(response.getBody().detalhes().toString()).contains("email");
        assertThat(response.getBody().detalhes().toString()).contains("Formato de e-mail inválido.");
    }
}