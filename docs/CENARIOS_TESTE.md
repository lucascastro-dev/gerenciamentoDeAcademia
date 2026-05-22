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

---

## Reiniciar massa de demo

- Turmas demo: exclua manualmente as que começam com `[Demo]` em **Gerenciar turmas**, reinicie o backend para recriar.
- Itens de programação: exclua pelos títulos na aba Itens; reinicie para o seed recriar os que faltam.
- Banco limpo: subir com profile `local`/`docker` do zero recria tudo.
