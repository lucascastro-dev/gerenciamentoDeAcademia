-- Programação: itens por turma (aluno opcional) — idempotente
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS turma_id BIGINT REFERENCES tb_turma(id);
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS data_fim DATE;
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS hora_inicio TIME;
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS hora_fim TIME;
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS sala VARCHAR(255);
ALTER TABLE tb_item_programacao_aluno ALTER COLUMN aluno_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_item_prog_instituicao ON tb_item_programacao_aluno(instituicao_id);
CREATE INDEX IF NOT EXISTS idx_item_prog_aluno ON tb_item_programacao_aluno(aluno_id);
CREATE INDEX IF NOT EXISTS idx_item_prog_turma ON tb_item_programacao_aluno(turma_id);
