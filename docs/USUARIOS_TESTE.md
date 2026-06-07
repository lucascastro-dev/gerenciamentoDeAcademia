# Usuários de teste (ambiente local / docker)

Instituição: **Instituição Master** (mesmo vínculo do master: ID `1` no login)

Senha dos perfis de teste (colaboradores): **`123`**

**Operador master da plataforma:** CPF `00000000191` — senha **`Master@2024!`** — vínculo **Plataforma — Operação master** (ID `0`). Não use a instituição ID `1` para operação cross-tenant.

**Mensalidade do aluno:** valor e dia de vencimento são **por instituição** (`tb_matricula_instituicao`). Na consulta master, edite em cada bloco de matrícula.

**Portal do aluno (teste):**

| Nome | CPF | Senha |
|------|-----|-------|
| Teste Portal Aluno | 12345678909 | 123456 (6 primeiros dígitos do CPF) |

Login: informe o CPF, escolha **Instituição Master** (só aparece se o aluno estiver vinculado à instituição) e senha **`123456`**.

Após matricular qualquer aluno novo, o portal usa automaticamente os **6 primeiros dígitos do CPF** como senha inicial. O aluno é vinculado à instituição da sessão do colaborador. Altere em **Portal do aluno → Alterar senha** (mínimo 4 caracteres).

**Cobrança em atraso:** até **5 dias** após o vencimento o acesso continua, com **popup de alerta** após o login. Depois disso o login é bloqueado com orientação para contatar o **SAC**. O aluno de teste é ajustado no startup (`local`/`docker`) com vencimento há poucos dias e sem pagamento no mês atual, para cair na tolerância e exibir o alerta.

**Minha programação (portal aluno):** itens e turmas de demonstração (conflitos na grade). Roteiro completo: [CENARIOS_TESTE.md](./CENARIOS_TESTE.md).

**Migração banco (Academia → Instituicao):** se o banco já existia, execute `docs/MIGRACAO_ACADEMIA_INSTITUICAO.sql` antes de subir a nova versão.

**CPF duplicado (legado):** se a matrícula antiga criou mais de um registro por CPF, execute `docs/MIGRACAO_DEDUP_ALUNO_CPF.sql`.

| Perfil | CPF | Nome |
|--------|-----|------|
| Master (já existente) | 00000000191 | Administrador Master |
| Financeiro | 52998224725 | Teste Financeiro |
| RH | 71428793860 | Teste RH |
| TI | 39053344705 | Teste TI |
| Administrador | 94325755004 | Teste Administrador |
| Recepção | 86833851085 | Teste Recepção |
| Professor | 61482582007 | Teste Professor |
| Estagiário | 58236123030 | Teste Estagiário |
| Serviços gerais | 74572288020 | Teste Serviços Gerais |
| Terceirizado | 45449941013 | Teste Terceirizado |

No login: informe o CPF, aguarde carregar a instituição e use a senha `123` (ou a do master).

---

## Perfil Professor (`61482582007`)

Após alterações de permissão, faça **logout e login** para renovar o JWT.

### Menus visíveis

| Seção | Itens |
|-------|-------|
| Geral | Início, Meu cadastro |
| Área do professor | Minhas turmas, Presença, Gerar certificados |
| Acadêmico | Consultar alunos, Programação e grade |

### Oculto para professor

Consultar turmas (instituição), Cadastrar turma, Matricular aluno, Financeiro, Administrativo.

### Turmas seedadas do professor

- `[Demo] Judô — turma portal` — Segunda/Quarta, Dojo 1
- `[Demo] Karatê — conflito Dojo 1` — Terça/Quinta, Dojo 1

Roteiro detalhado: [CENARIOS_TESTE.md](./CENARIOS_TESTE.md) (seções 6–10).
