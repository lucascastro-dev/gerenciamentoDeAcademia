-- 1. Limpa dados antigos (opcional para teste limpo)
DELETE FROM academia_funcionario;
DELETE FROM tb_funcionario;
DELETE FROM tb_academia;

-- 2. Insere a Academia
INSERT INTO tb_academia(id, razao_social, cnpj)
VALUES (1, 'Academia Central', '23498897000120');

-- Use este INSERT no seu data.sql (A senha é 123456)
INSERT INTO tb_funcionario(id, nome, cpf, cargo, senha, cadastro_ativo)
VALUES (1, 'Lucas', '15179950783', 'Professor', '123456', false);

-- 4. Cria o Vínculo (Obrigatório para o JOIN funcionar)
INSERT INTO academia_funcionario(academia_id, funcionario_id) VALUES (1, 1);

ALTER TABLE tb_funcionario ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM tb_funcionario);