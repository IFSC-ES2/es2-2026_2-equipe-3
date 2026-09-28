-- V1: Criação da estrutura base (US02)

CREATE TABLE usuarios (
                          id BIGINT AUTO_INCREMENT NOT NULL,
                          nome VARCHAR(100) NOT NULL,
                          email VARCHAR(100) NOT NULL,
                          senha VARCHAR(45),
                          matricula VARCHAR(45),
                          PRIMARY KEY (id),
                          CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE orientadores (
                              id BIGINT NOT NULL,
                              departamento VARCHAR(100),
                              ativo BOOLEAN NOT NULL DEFAULT TRUE,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_orientador_usuario FOREIGN KEY (id) REFERENCES usuarios (id)
);

CREATE TABLE perfis_professores (
                                    id BIGINT AUTO_INCREMENT NOT NULL,
                                    vagas_disponiveis INT NOT NULL,
                                    biografia VARCHAR(500),
                                    orientador_id BIGINT,
                                    PRIMARY KEY (id),
                                    CONSTRAINT uk_perfil_orientador UNIQUE (orientador_id),
                                    CONSTRAINT fk_perfil_orientador FOREIGN KEY (orientador_id) REFERENCES orientadores (id)
);

CREATE TABLE linhas_pesquisa (
                                 id BIGINT AUTO_INCREMENT NOT NULL,
                                 nome VARCHAR(80),
                                 descricao TEXT,
                                 perfil_id BIGINT,
                                 PRIMARY KEY (id),
                                 CONSTRAINT fk_linha_perfil FOREIGN KEY (perfil_id) REFERENCES perfis_professores (id)
);