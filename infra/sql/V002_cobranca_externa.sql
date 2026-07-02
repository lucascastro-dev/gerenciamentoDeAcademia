-- Turma360: cobranças externas (Asaas / modo local)
CREATE TABLE IF NOT EXISTS tb_cobranca_externa (
    id BIGSERIAL PRIMARY KEY,
    instituicao_id BIGINT NOT NULL REFERENCES tb_instituicao(id),
    cpf_aluno VARCHAR(11),
    tipo VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    valor NUMERIC(12, 2),
    mes_competencia INTEGER,
    ano_competencia INTEGER,
    id_externo VARCHAR(80),
    url_pagamento VARCHAR(500),
    provedor VARCHAR(40),
    referencia VARCHAR(40),
    criado_em TIMESTAMP,
    pago_em TIMESTAMP
);
