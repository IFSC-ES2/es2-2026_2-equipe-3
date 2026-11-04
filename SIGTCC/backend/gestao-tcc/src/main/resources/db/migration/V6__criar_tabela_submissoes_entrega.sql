-- V6: Criação da tabela de submissões de entrega por link externo (US06 / ADR-0013)
CREATE TABLE submissoes_entrega (
    id BIGINT AUTO_INCREMENT NOT NULL,
    aluno_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    enviado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_submissao_aluno FOREIGN KEY (aluno_id) REFERENCES alunos (id)
);