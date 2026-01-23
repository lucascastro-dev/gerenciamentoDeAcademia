-- 1. Limpa dados antigos (opcional para teste limpo)
DELETE FROM academia_funcionario;
DELETE FROM tb_funcionario;
DELETE FROM tb_academia;

-- 2. Insere a Academia
INSERT INTO tb_academia(id, razao_social, cnpj)
VALUES (1, 'Academia Central', '23498897000120');

-- Use este INSERT no seu data.sql (A senha é 123456)
INSERT INTO tb_funcionario(id, nome, cpf, cargo, senha, cadastro_ativo)
VALUES (1, 'Lucas', '15179950783', 'Professor', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqCYAdVqK9qSbs6.H7shE.t0KSe6', true);

-- 4. Cria o Vínculo (Obrigatório para o JOIN funcionar)
INSERT INTO academia_funcionario(academia_id, funcionario_id) VALUES (1, 1);