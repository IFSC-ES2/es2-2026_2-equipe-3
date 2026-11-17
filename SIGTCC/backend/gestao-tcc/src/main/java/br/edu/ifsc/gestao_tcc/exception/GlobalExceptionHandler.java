package br.edu.ifsc.gestao_tcc.exception;

import br.edu.ifsc.gestao_tcc.dto.ErroCampoDTO;
import br.edu.ifsc.gestao_tcc.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;

@Slf4j
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

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                request.getRequestURI(),
                "Os dados foram alterados por outra operação ao mesmo tempo. Atualize a página e tente novamente."
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

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleAcessoNegadoException(
            AcessoNegadoException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ValidacaoRequisicaoException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidacaoRequisicaoException(
            ValidacaoRequisicaoException ex, HttpServletRequest request) {

        List<ErroCampoDTO> detalhes = List.of(
                new ErroCampoDTO(ex.getCampo(), ex.getMessage())
        );

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos dados enviados",
                request.getRequestURI(),
                detalhes
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Violação de integridade em {}: {}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.CONFLICT.value(),
                "Conflito de dados",
                request.getRequestURI(),
                "A operação viola uma restrição de integridade dos dados, como um valor que já está cadastrado."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Último recurso: só é usado quando nenhum handler mais específico se aplica.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception ex, HttpServletRequest request) {

        if (ex instanceof ErrorResponse respostaDoSpring) {
            HttpStatusCode status = respostaDoSpring.getStatusCode();
            String erro = status.value() == HttpStatus.NOT_FOUND.value()
                    ? "Recurso não encontrado"
                    : "Requisição inválida";

            ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                    status.value(),
                    erro,
                    request.getRequestURI(),
                    respostaDoSpring.getBody().getDetail()
            );

            return ResponseEntity.status(status).body(errorResponse);
        }

        log.error("Erro inesperado em {}", request.getRequestURI(), ex);

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                request.getRequestURI(),
                "Ocorreu um erro inesperado. Tente novamente mais tarde."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
