package br.edu.ifsc.gestao_tcc.exception;

public class ValidacaoRequisicaoException extends RuntimeException {

    private final String campo;

    public ValidacaoRequisicaoException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
