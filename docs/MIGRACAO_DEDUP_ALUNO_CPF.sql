-- Remove alunos duplicados por CPF (mantém o menor id) e reassocia turmas.
-- Execute antes de subir a versão com UNIQUE em tb_aluno.cpf, se a matrícula duplicou registros.

BEGIN;

CREATE TEMP TABLE aluno_cpf_manter AS
SELECT cpf, MIN(id) AS id_manter
FROM tb_aluno
GROUP BY cpf;

UPDATE turma_aluno ta
SET aluno_id = m.id_manter
FROM tb_aluno a
JOIN aluno_cpf_manter m ON m.cpf = a.cpf
WHERE ta.aluno_id = a.id
  AND a.id <> m.id_manter;

DELETE FROM tb_aluno a
USING aluno_cpf_manter m
WHERE a.cpf = m.cpf
  AND a.id <> m.id_manter;

-- Garante índice único (Hibernate ddl-auto=update também pode criar)
CREATE UNIQUE INDEX IF NOT EXISTS uk_tb_aluno_cpf ON tb_aluno (cpf);

COMMIT;
