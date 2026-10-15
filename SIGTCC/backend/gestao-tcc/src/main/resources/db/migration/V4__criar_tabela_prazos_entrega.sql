-- V4: Criação da tabela de prazos de entrega por Turma_TCC

CREATE TABLE prazos_entrega (
                                id BIGINT AUTO_INCREMENT NOT NULL,
                                turma_id BIGINT NOT NULL,
                                tipo VARCHAR(30) NOT NULL,
                                data_limite DATE NOT NULL,
                                criado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
                                atualizado_em DATETIME(6),
                                PRIMARY KEY (id),
                                CONSTRAINT uk_prazo_turma_tipo UNIQUE (turma_id, tipo),
                                CONSTRAINT fk_prazo_turma FOREIGN KEY (turma_id) REFERENCES turmas_tcc (id)
);
