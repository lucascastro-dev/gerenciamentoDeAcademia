-- Massa de demonstração (idempotente) — instituição Master CNPJ 00000000000191, aluno 12345678909
-- Execute após migrações de schema.

INSERT INTO tb_sala (instituicao_id, nome, capacidade, ativa)
SELECT i.id, v.nome, v.capacidade, TRUE
FROM tb_instituicao i
CROSS JOIN (VALUES ('Dojo 1', 30), ('Sala 2', 25), ('Laboratorio', 20)) AS v(nome, capacidade)
WHERE i.cnpj = '00000000000191'
  AND NOT EXISTS (
    SELECT 1 FROM tb_sala s WHERE s.instituicao_id = i.id AND s.nome = v.nome
  );

INSERT INTO tb_turma (horario, hora_inicio, hora_fim, sala, modalidade, instituicao_id, professor_id)
SELECT '18:00-19:30', TIME '18:00', TIME '19:30', 'Dojo 1', '[Demo] Judô — turma portal', i.id, f.id
FROM tb_instituicao i
LEFT JOIN tb_funcionario f ON f.cpf = '61482582007'
WHERE i.cnpj = '00000000000191'
  AND NOT EXISTS (SELECT 1 FROM tb_turma t WHERE t.modalidade = '[Demo] Judô — turma portal');

INSERT INTO turma_dias (turma_id, dias)
SELECT t.id, 'Segunda' FROM tb_turma t WHERE t.modalidade = '[Demo] Judô — turma portal'
  AND NOT EXISTS (SELECT 1 FROM turma_dias d WHERE d.turma_id = t.id AND d.dias = 'Segunda');
INSERT INTO turma_dias (turma_id, dias)
SELECT t.id, 'Quarta' FROM tb_turma t WHERE t.modalidade = '[Demo] Judô — turma portal'
  AND NOT EXISTS (SELECT 1 FROM turma_dias d WHERE d.turma_id = t.id AND d.dias = 'Quarta');

INSERT INTO tb_turma (horario, hora_inicio, hora_fim, sala, modalidade, instituicao_id, professor_id)
SELECT '18:00-19:30', TIME '18:00', TIME '19:30', 'Dojo 1', '[Demo] Karatê — conflito Dojo 1', i.id, f.id
FROM tb_instituicao i
LEFT JOIN tb_funcionario f ON f.cpf = '61482582007'
WHERE i.cnpj = '00000000000191'
  AND NOT EXISTS (SELECT 1 FROM tb_turma t WHERE t.modalidade = '[Demo] Karatê — conflito Dojo 1');

INSERT INTO turma_dias (turma_id, dias)
SELECT t.id, 'Terça' FROM tb_turma t WHERE t.modalidade = '[Demo] Karatê — conflito Dojo 1'
  AND NOT EXISTS (SELECT 1 FROM turma_dias d WHERE d.turma_id = t.id AND d.dias = 'Terça');
INSERT INTO turma_dias (turma_id, dias)
SELECT t.id, 'Quinta' FROM tb_turma t WHERE t.modalidade = '[Demo] Karatê — conflito Dojo 1'
  AND NOT EXISTS (SELECT 1 FROM turma_dias d WHERE d.turma_id = t.id AND d.dias = 'Quinta');

INSERT INTO tb_turma (horario, hora_inicio, hora_fim, sala, modalidade, instituicao_id)
SELECT '19:00-20:00', TIME '19:00', TIME '20:00', 'Sala 2', '[Demo] Pilates — Sala 2', i.id
FROM tb_instituicao i
WHERE i.cnpj = '00000000000191'
  AND NOT EXISTS (SELECT 1 FROM tb_turma t WHERE t.modalidade = '[Demo] Pilates — Sala 2');

INSERT INTO turma_dias (turma_id, dias)
SELECT t.id, 'Sexta' FROM tb_turma t WHERE t.modalidade = '[Demo] Pilates — Sala 2'
  AND NOT EXISTS (SELECT 1 FROM turma_dias d WHERE d.turma_id = t.id AND d.dias = 'Sexta');

-- Vincular aluno de teste à turma Judô demo
INSERT INTO turma_aluno (turma_id, aluno_id)
SELECT t.id, a.id
FROM tb_turma t, tb_aluno a
WHERE t.modalidade = '[Demo] Judô — turma portal' AND a.cpf = '12345678909'
  AND NOT EXISTS (
    SELECT 1 FROM turma_aluno ta WHERE ta.turma_id = t.id AND ta.aluno_id = a.id
  );

-- Atualizar turmas legadas sem instituição
UPDATE tb_turma SET instituicao_id = (SELECT id FROM tb_instituicao WHERE cnpj = '00000000000191' LIMIT 1)
WHERE instituicao_id IS NULL;

-- Programação do aluno (datas relativas à semana atual)
INSERT INTO tb_item_programacao_aluno (instituicao_id, aluno_id, tipo, titulo, descricao, data_prevista, horario, hora_inicio, hora_fim, sala)
SELECT i.id, a.id, 'AULA', 'Judô — aula extra (portal)', 'Aula avulsa na Minha programação.', CURRENT_DATE + 7, '18:00-19:30', TIME '18:00', TIME '19:30', 'Dojo 1'
FROM tb_instituicao i, tb_aluno a
WHERE i.cnpj = '00000000000191' AND a.cpf = '12345678909'
  AND NOT EXISTS (SELECT 1 FROM tb_item_programacao_aluno p WHERE p.titulo = 'Judô — aula extra (portal)' AND p.aluno_id = a.id);

INSERT INTO tb_item_programacao_aluno (instituicao_id, aluno_id, tipo, titulo, descricao, data_prevista, horario, hora_inicio, hora_fim, sala)
SELECT i.id, a.id, 'PROVA', 'Prova faixa amarela — Dojo 1 (conflito)', 'Simulado de prova com conflito na grade.', date_trunc('week', CURRENT_DATE)::date, '18:00-19:00', TIME '18:00', TIME '19:00', 'Dojo 1'
FROM tb_instituicao i, tb_aluno a
WHERE i.cnpj = '00000000000191' AND a.cpf = '12345678909'
  AND NOT EXISTS (SELECT 1 FROM tb_item_programacao_aluno p WHERE p.titulo = 'Prova faixa amarela — Dojo 1 (conflito)' AND p.aluno_id = a.id);

INSERT INTO tb_item_programacao_aluno (instituicao_id, aluno_id, tipo, titulo, descricao, data_prevista, horario, hora_inicio, hora_fim, sala)
SELECT i.id, a.id, 'SERIE_TREINO', 'Serie de treino — forca', 'Treino complementar.', date_trunc('week', CURRENT_DATE)::date + 2, '20:00-21:00', TIME '20:00', TIME '21:00', 'Laboratorio'
FROM tb_instituicao i, tb_aluno a
WHERE i.cnpj = '00000000000191' AND a.cpf = '12345678909'
  AND NOT EXISTS (SELECT 1 FROM tb_item_programacao_aluno p WHERE p.titulo = 'Série de treino — força' AND p.aluno_id = a.id);

INSERT INTO tb_item_programacao_aluno (instituicao_id, aluno_id, tipo, titulo, descricao, data_prevista, horario, hora_inicio, hora_fim, sala)
SELECT i.id, a.id, 'EVENTO', 'Open day — recepção', 'Evento institucional.', date_trunc('week', CURRENT_DATE)::date + 5, '10:00-12:00', TIME '10:00', TIME '12:00', 'Sala 2'
FROM tb_instituicao i, tb_aluno a
WHERE i.cnpj = '00000000000191' AND a.cpf = '12345678909'
  AND NOT EXISTS (SELECT 1 FROM tb_item_programacao_aluno p WHERE p.titulo = 'Open day — recepção' AND p.aluno_id = a.id);
