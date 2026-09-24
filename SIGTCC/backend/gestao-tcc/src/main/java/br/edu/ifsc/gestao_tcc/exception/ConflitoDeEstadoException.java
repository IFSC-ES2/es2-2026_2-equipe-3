package br.edu.ifsc.gestao_tcc.exception;

/**
 * Exceção base para conflitos de estado: a requisição é válida e a regra de
 * negócio permitiria a operação em outro momento, mas o estado atual do
 * recurso impede a ação agora (ex.: recurso já existe, já foi respondido).
 * Mapeada pelo {@link GlobalExceptionHandler} para HTTP 409 Conflict.
 *
 * Ex.: solicitação pendente duplicada, solicitação já respondida.
 * {@link EmailDuplicadoException} permanece separada por já ter handler
 * próprio e testes existentes na US02; as novas exceções de conflito da
 * US04 estendem esta classe.
 */
public class ConflitoDeEstadoException extends RuntimeException {
    public ConflitoDeEstadoException(String message) {
        super(message);
    }
}
