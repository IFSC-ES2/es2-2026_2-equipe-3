-- V6: Criação da tabela de submissões de entregas por link externo (US06 / ADR-0013)
-- Não há coluna de arquivo/binário: apenas a URL informada pelo aluno é persistida.

CREATE TABLE submissoes (
                             id BIGINT AUTO_INCREMENT NOT NULL,
                             aluno_id BIGINT NOT NULL,
                             tipo VARCHAR(30) NOT NULL,
                             url_documento VARCHAR(2048) NOT NULL,
                             enviado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
                             PRIMARY KEY (id),
                             CONSTRAINT fk_submissao_aluno FOREIGN KEY (aluno_id) REFERENCES alunos (id)
);
