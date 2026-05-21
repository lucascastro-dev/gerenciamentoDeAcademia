DELETE FROM academia_funcionario;
DELETE FROM tb_funcionario;
DELETE FROM tb_academia;
DELETE FROM tb_usuarios;

INSERT INTO tb_academia(id, razao_social, cnpj, cadastro_ativo)
VALUES (1, 'Academia Central', '23498897000120', true);

INSERT INTO tb_funcionario(id, nome, cpf, cargo, tipo_funcionario, especializacao, senha, cadastro_ativo, permitir_gerenciar_funcoes)
VALUES (1, 'Lucas', '15179950783', 'Professor', 'PROFESSOR', 'Judô', '123456', false, false);

INSERT INTO academia_funcionario(academia_id, funcionario_id) VALUES (1, 1);
