-- Turma360: colunas para anexo PDF em documentos de remuneração
-- Executar em produção quando spring.jpa.hibernate.ddl-auto=validate

ALTER TABLE tb_documento_remuneracao_colaborador
    ADD COLUMN IF NOT EXISTS caminho_arquivo VARCHAR(500);

ALTER TABLE tb_documento_remuneracao_colaborador
    ADD COLUMN IF NOT EXISTS nome_arquivo_original VARCHAR(255);
