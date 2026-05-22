-- Migração: nomenclatura Academia → Instituição (PostgreSQL)
-- Execute com o backend parado. Faça backup antes.
-- Após executar, reinicie a aplicação (ddl-auto=update pode ajustar colunas restantes).

ALTER TABLE IF EXISTS tb_academia RENAME TO tb_instituicao;

ALTER TABLE IF EXISTS academia_funcionario RENAME TO instituicao_funcionario;
ALTER TABLE IF EXISTS instituicao_funcionario RENAME COLUMN academia_id TO instituicao_id;

ALTER TABLE IF EXISTS tb_assinatura_plataforma RENAME COLUMN academia_id TO instituicao_id;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'tb_turma' AND column_name = 'academia_id'
  ) THEN
    ALTER TABLE tb_turma RENAME COLUMN academia_id TO instituicao_id;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'tb_item_programacao_aluno' AND column_name = 'academia_id'
  ) THEN
    ALTER TABLE tb_item_programacao_aluno RENAME COLUMN academia_id TO instituicao_id;
  END IF;
END $$;
