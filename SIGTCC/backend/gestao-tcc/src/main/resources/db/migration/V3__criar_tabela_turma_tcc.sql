-- V3: Criação da entidade Turma_TCC e vínculo com professor responsável (US01 / Issue #128)

CREATE TABLE turmas_tcc (
    id BIGINT AUTO_INCREMENT NOT NULL,
    semestre VARCHAR(20) NOT NULL,
    vigente BOOLEAN NOT NULL DEFAULT FALSE,
    professor_responsavel_id BIGINT NOT NULL,
    criado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_turma_professor_responsavel FOREIGN KEY (professor_responsavel_id) REFERENCES orientadores (id)
);
