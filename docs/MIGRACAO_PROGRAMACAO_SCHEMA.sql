-- Complemento: grade horária, salas e programação (PostgreSQL, idempotente)
-- Execute após MIGRACAO_ACADEMIA_INSTITUICAO.sql

-- Turma: vínculo instituição + horário estruturado + sala
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'tb_turma' AND column_name = 'academia_id'
  ) THEN
    ALTER TABLE tb_turma RENAME COLUMN academia_id TO instituicao_id;
  END IF;
END $$;

ALTER TABLE tb_turma ADD COLUMN IF NOT EXISTS instituicao_id BIGINT;
ALTER TABLE tb_turma ADD COLUMN IF NOT EXISTS hora_inicio TIME;
ALTER TABLE tb_turma ADD COLUMN IF NOT EXISTS hora_fim TIME;
ALTER TABLE tb_turma ADD COLUMN IF NOT EXISTS sala VARCHAR(255);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_name = 'fk_turma_instituicao' AND table_name = 'tb_turma'
  ) THEN
    ALTER TABLE tb_turma
      ADD CONSTRAINT fk_turma_instituicao
      FOREIGN KEY (instituicao_id) REFERENCES tb_instituicao(id);
  END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Salas
CREATE TABLE IF NOT EXISTS tb_sala (
  id BIGSERIAL PRIMARY KEY,
  instituicao_id BIGINT NOT NULL REFERENCES tb_instituicao(id),
  nome VARCHAR(255) NOT NULL,
  capacidade INTEGER,
  ativa BOOLEAN DEFAULT TRUE
);

-- Itens Minha programação
CREATE TABLE IF NOT EXISTS tb_item_programacao_aluno (
  id BIGSERIAL PRIMARY KEY,
  instituicao_id BIGINT NOT NULL REFERENCES tb_instituicao(id),
  aluno_id BIGINT REFERENCES tb_aluno(id),
  turma_id BIGINT REFERENCES tb_turma(id),
  tipo VARCHAR(50) NOT NULL,
  titulo VARCHAR(255) NOT NULL,
  descricao VARCHAR(2000),
  data_prevista DATE,
  data_fim DATE,
  horario VARCHAR(255),
  hora_inicio TIME,
  hora_fim TIME,
  sala VARCHAR(255)
);

ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS turma_id BIGINT REFERENCES tb_turma(id);
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS data_fim DATE;
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS hora_inicio TIME;
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS hora_fim TIME;
ALTER TABLE tb_item_programacao_aluno ADD COLUMN IF NOT EXISTS sala VARCHAR(255);
ALTER TABLE tb_item_programacao_aluno ALTER COLUMN aluno_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_item_prog_instituicao ON tb_item_programacao_aluno(instituicao_id);
CREATE INDEX IF NOT EXISTS idx_item_prog_aluno ON tb_item_programacao_aluno(aluno_id);
