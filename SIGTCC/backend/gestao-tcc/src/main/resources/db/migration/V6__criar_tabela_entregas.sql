-- V6: Criação da tabela de entregas de TCC (US06 / Issue #208 / ADR-0013)

CREATE TABLE entregas (
    id BIGINT AUTO_INCREMENT NOT NULL,
    aluno_id BIGINT NOT NULL,
    orientador_id BIGINT NOT NULL,
    turma_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    url_documento VARCHAR(2048) NOT NULL,
    atrasada BOOLEAN NOT NULL DEFAULT FALSE,
    enviado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    atualizado_em DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_entrega_aluno FOREIGN KEY (aluno_id) REFERENCES alunos (id),
    CONSTRAINT fk_entrega_orientador FOREIGN KEY (orientador_id) REFERENCES orientadores (id),
    CONSTRAINT fk_entrega_turma FOREIGN KEY (turma_id) REFERENCES turmas_tcc (id),
    CONSTRAINT uk_entrega_aluno_turma_tipo UNIQUE (aluno_id, turma_id, tipo)
);

