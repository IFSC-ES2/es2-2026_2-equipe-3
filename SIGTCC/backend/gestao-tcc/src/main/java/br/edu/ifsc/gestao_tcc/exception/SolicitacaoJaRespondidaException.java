package br.edu.ifsc.gestao_tcc.exception;

public class SolicitacaoJaRespondidaException extends ConflitoDeEstadoException {
    public SolicitacaoJaRespondidaException(String message) {
        super(message);
    }
}
