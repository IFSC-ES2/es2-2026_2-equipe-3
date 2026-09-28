-- V2: Criação das entidades Aluno e Solicitação de Orientação (US04)

-- Criação da tabela alunos (herda de usuarios)
CREATE TABLE alunos (
                        id BIGINT NOT NULL,
                        curso VARCHAR(100),
                        PRIMARY KEY (id),
                        CONSTRAINT fk_aluno_usuario FOREIGN KEY (id) REFERENCES usuarios (id)
);

-- Criação da tabela solicitacoes_orientacao
CREATE TABLE solicitacoes_orientacao (
                                         id BIGINT AUTO_INCREMENT NOT NULL,
                                         orientador_id BIGINT NOT NULL,
                                         aluno_id BIGINT NOT NULL,
                                         tema VARCHAR(150) NOT NULL,
                                         mensagem VARCHAR(1000) NOT NULL,
                                         justificativa VARCHAR(500),
                                         status VARCHAR(50) NOT NULL,
                                         criado_em DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
                                         PRIMARY KEY (id),
                                         CONSTRAINT fk_solicitacao_orientador FOREIGN KEY (orientador_id) REFERENCES orientadores (id),
                                         CONSTRAINT fk_solicitacao_aluno FOREIGN KEY (aluno_id) REFERENCES alunos (id)
);