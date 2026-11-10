-- V7: Criação da tabela de pareceres de avaliação de entregas (US07 / Issue #215)

CREATE TABLE pareceres (
    id BIGINT AUTO_INCREMENT NOT NULL,
    entrega_id BIGINT NOT NULL,
    orientador_id BIGINT NOT NULL,
    resultado VARCHAR(30) NOT NULL,
    comentario VARCHAR(2000),
    registrado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_parecer_entrega FOREIGN KEY (entrega_id) REFERENCES entregas (id),
    CONSTRAINT fk_parecer_orientador FOREIGN KEY (orientador_id) REFERENCES orientadores (id)
);

