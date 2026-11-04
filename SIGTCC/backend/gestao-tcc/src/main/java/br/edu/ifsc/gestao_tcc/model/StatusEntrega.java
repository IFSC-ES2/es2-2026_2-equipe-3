package br.edu.ifsc.gestao_tcc.model;

import lombok.Getter;

@Getter
public enum StatusEntrega {
    SUBMETIDO("Submetido"),
    AJUSTES_NECESSARIOS("Ajustes Necessários"),
    APROVADO("Aprovado"),
    REPROVADO("Reprovado");

    private final String descricao;

    StatusEntrega(String descricao) {
        this.descricao = descricao;
    }
}

