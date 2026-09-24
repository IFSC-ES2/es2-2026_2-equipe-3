package br.edu.ifsc.gestao_tcc.exception;

import br.edu.ifsc.gestao_tcc.dto.ErroCampoDTO;
import br.edu.ifsc.gestao_tcc.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            HttpStatus.NOT_FOUND.value(),
            "Recurso não encontrado",
            request.getRequestURI(),
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailDuplicadoException(
            EmailDuplicadoException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ConflitoDeEstadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflitoDeEstadoException(
            ConflitoDeEstadoException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

@ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleRegraDeNegocioException(
            RegraDeNegocioException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Regra de negócio violada",
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErroCampoDTO> detalhesErros = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErroCampoDTO(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos dados enviados",
                request.getRequestURI(),
                detalhesErros
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição inválido ou ilegível",
                request.getRequestURI(),
                "O corpo da requisição está ausente, mal formatado ou contém um valor incompatível com o tipo esperado."
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String nomeParametro = ex.getName();
        String valoresAceitos = ex.getRequiredType() != null && ex.getRequiredType().isEnum()
                ? String.join(", ", Arrays.stream(ex.getRequiredType().getEnumConstants())
                        .map(Object::toString)
                        .toArray(String[]::new))
                : null;

        String mensagem = valoresAceitos != null
                ? "Valor inválido para o parâmetro '" + nomeParametro + "'. Valores aceitos: " + valoresAceitos + "."
                : "Valor inválido para o parâmetro '" + nomeParametro + "'.";

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro de requisição inválido",
                request.getRequestURI(),
                mensagem
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
