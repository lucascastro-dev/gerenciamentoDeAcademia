# Cenários de teste (massa automática)

Perfis **local** e **docker**: ao subir o backend, os dados abaixo são criados automaticamente (idempotente — não duplica se já existir).

Documentação de usuários: [USUARIOS_TESTE.md](./USUARIOS_TESTE.md)

---

## Salas (instituição Master)

| Sala | Capacidade |
|------|------------|
| Dojo 1 | 30 |
| Sala 2 | 25 |
| Laboratório | 20 |

---

## Turmas de demonstração

| Modalidade | Dias | Horário | Sala | Professor |
|------------|------|---------|------|-----------|
| `[Demo] Judô — turma portal` | Segunda, Quarta | 18:00–19:30 | Dojo 1 | Teste Professor |
| `[Demo] Karatê — conflito Dojo 1` | Terça, Quinta | 18:00–19:30 | Dojo 1 | Teste Professor |
| `[Demo] Pilates — Sala 2` | Sexta | 19:00–20:00 | Sala 2 | — |

O aluno **Teste Portal Aluno** (`12345678909`) fica vinculado à turma de Judô demo.

---

## Minha programação (aluno `12345678909`)

| Título | Tipo | Quando (semana atual) | Horário | Sala |
|--------|------|------------------------|---------|------|
| Judô — aula extra (portal) | Aula | +7 dias | 18:00–19:30 | Dojo 1 |
| Prova faixa amarela — Dojo 1 (conflito) | Prova | Segunda desta semana | 18:00–19:00 | Dojo 1 |
| Série de treino — força | Série de treino | Quarta desta semana | 20:00–21:00 | Laboratório |
| Open day — recepção | Evento | Sábado desta semana | 10:00–12:00 | Sala 2 |

---

## Roteiro de testes manuais

### 1. Cadastro de turma (horário real)

1. Login: **Administrador** `94325755004` / senha `123` / Instituição Master.
2. Menu **Acadêmico → Gerenciar turmas**.
3. Preencha modalidade, **Início** e **Término** com os campos de horário (não texto livre).
4. Escolha sala no combo (se as salas já foram seedadas).
5. Marque dias e **Criar turma**.
6. Confira na lista o horário no formato `18:00-19:30`.

### 2. Grade horária e conflitos

1. Mesmo usuário (ou Diretor `00000000191` com senha master).
2. **Acadêmico → Programação e grade → aba Grade horária**.
3. Navegue a semana atual: deve aparecer turmas `[Demo]` e itens de programação.
4. Eventos na **Dojo 1** com sobreposição (turmas Terça/Quinta + prova na segunda, etc.) devem aparecer com destaque de **conflito** (vermelho).

### 3. CRUD programação

1. Aba **Itens (Minha programação)**.
2. Crie item para aluno `12345678909`, mesma sala/horário de uma turma → **Verificar conflito** antes de salvar.
3. Edite ou exclua um item demo.

### 4. Portal do aluno

1. Login: `12345678909` / `123` / Instituição Master.
2. **Minha programação**: cards por tipo (prova, aula, série, evento) em ordem de data.
3. Confira que os 4 itens da tabela acima aparecem.

### 5. Salas

1. **Programação e grade → Salas**: liste as 3 salas seedadas; cadastre uma nova e use no formulário de turma/programação.

### 6. Perfil Professor — Minhas turmas

1. Login: **Professor** `61482582007` / senha `123` / Instituição Master. **Re-login** após deploy para carregar permissões no JWT.
2. Menu **Área do professor → Minhas turmas**.
3. Confira turmas `[Demo] Judô` e `[Demo] Karatê` (professor seedado): modalidade, sala, horário, dias e total de alunos.
4. Abra uma turma e **adicione** um aluno matriculado na instituição (ex.: `12345678909`).
5. **Remova** o aluno da turma (não desmatricula da instituição).
6. Tente CPF de aluno não matriculado → mensagem de erro.

### 7. Perfil Professor — Presença

1. Mesmo login do professor.
2. **Área do professor → Presença**: selecione turma `[Demo] Karatê` (Terça/Quinta).
3. Navegue o mês: colunas devem aparecer nos dias de aula (não vazio).
4. Marque P/F/J/A para alguns alunos e **Salvar**.
5. **Gerar PDF** e confira o arquivo baixado.

### 8. Perfil Professor — Gerar certificados

1. **Área do professor → Gerar certificados**.
2. Professor já preenchido com o usuário logado (sem dropdown).
3. Informe alunos/faixas e gere o resumo TXT; download automático.
4. Gere outro envio parcial → novo arquivo com sufixo data/hora (sem sobrescrever).

### 9. Perfil Professor — Consultar alunos

1. **Acadêmico → Consultar alunos**.
2. Busque `12345678909`: dados mascarados (CPF, telefone, e-mail).
3. Endereço exibido nos campos (JSON parseado).
4. Sem aba financeira; sem botões Salvar / Desmatricular / Matricular.

### 10. Perfil Professor — Programação e grade

1. **Acadêmico → Programação e grade**.
2. Aba **Itens**: criar, editar e excluir item de programação.
3. Aba **Grade horária**: visualização somente leitura.
4. Aba **Salas**: listagem somente leitura (sem cadastrar/excluir).

### 11. Operador master (regressão)

1. Login: `00000000191` / `Master@2024!` / vínculo Plataforma (`0`).
2. Consultar alunos cross-tenant, consultar turmas com filtros, matricular, instituições e financeiro plataforma.
3. Confirme que menus exclusivos do master permanecem invisíveis para o professor.

### 12. Folha de ponto (Colaborador → RH → Financeiro)

Documentação: [FOLHA_PONTO.md](./FOLHA_PONTO.md)

1. **Colaborador** (ex.: Professor `61482582007` / `123`): **Meu ponto** → **Registrar entrada** → **Registrar saída**. Confira tabela do mês e total de horas.
2. **RH** (`71428793860` / `123`): **Folha de ponto** → revisar horas → **Conferir folha de ponto** (falha se houver entrada sem saída).
3. **Financeiro** (`52998224725` / `123`): **Folha de pagamento** → **Integrar folha de ponto**.
4. **RH**: **Lançamento de holerite** → publicar para o colaborador testado.
5. **Financeiro**: **Confirmar pagamento** na folha (status Processado).
6. **Colaborador**: **Meu holerite** → visualizar holerite e recibo do mês.

---

## Reiniciar massa de demo

- Turmas demo: exclua manualmente as que começam com `[Demo]` em **Gerenciar turmas**, reinicie o backend para recriar.
- Itens de programação: exclua pelos títulos na aba Itens; reinicie para o seed recriar os que faltam.
- Banco limpo: subir com profile `local`/`docker` do zero recria tudo.
