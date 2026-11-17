-- V9: Cancela solicitações PENDENTE de orientadores já desativados (issue #263)
-- A partir desta versão, desativar um orientador cancela suas solicitações pendentes.
-- Esta migration aplica a mesma regra aos orientadores desativados antes da correção.

UPDATE solicitacoes_orientacao
SET status = 'CANCELADA'
WHERE status = 'PENDENTE'
  AND orientador_id IN (SELECT id FROM orientadores WHERE ativo = false);
