-- V5: Seed inicial de orientadores, perfis e turma vigente

INSERT INTO usuarios (id, nome, email)
VALUES 
  (1, 'Professor Orientador', 'professor.orientador@example.com'),
  (2, 'Professor Responsável pela Turma', 'professor.responsavel@example.com')
ON DUPLICATE KEY UPDATE email = VALUES(email);

INSERT INTO orientadores (id, departamento, ativo)
VALUES 
  (1, 'DAELN', true),
  (2, 'DAELN', true)
ON DUPLICATE KEY UPDATE ativo = true;

INSERT INTO perfis_professores (id, orientador_id, vagas_disponiveis, biografia)
VALUES 
  (1, 1, 3, 'Docente orientador de TCC.'),
  (2, 2, 3, 'Docente responsável pela turma e calendário de TCC.')
ON DUPLICATE KEY UPDATE vagas_disponiveis = VALUES(vagas_disponiveis);

INSERT INTO turmas_tcc (id, semestre, vigente, professor_responsavel_id)
VALUES 
  (1, '2026/2', true, 2)
ON DUPLICATE KEY UPDATE vigente = true, professor_responsavel_id = 2;