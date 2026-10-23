package br.edu.ifsc.gestao_tcc.model;

import lombok.Getter;

@Getter
public enum TipoEntrega {
    PROJETO_TCC("Projeto de TCC"),
    TCC1("TCC 1"),
    TCC2("TCC 2");

    private final String descricao;

    TipoEntrega(String descricao) {
        this.descricao = descricao;
    }

    public boolean vemAntesDe(TipoEntrega outro) {
        return this.ordinal() < outro.ordinal();
    }
}
