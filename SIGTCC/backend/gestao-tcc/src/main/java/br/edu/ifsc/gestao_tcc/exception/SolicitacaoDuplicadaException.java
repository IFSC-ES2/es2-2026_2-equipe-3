package br.edu.ifsc.gestao_tcc.exception;

public class SolicitacaoDuplicadaException extends ConflitoDeEstadoException {
    public SolicitacaoDuplicadaException(String message) {
        super(message);
    }
}
