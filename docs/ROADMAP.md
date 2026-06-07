# Roadmap — EduGestão Inteligente

Documento único de planejamento. Histórico na seção **Entregue**; prioridade atual em **Próxima fase**.

## Entregue (base + evolução até mai/2026)

### Infra e plataforma
- Docker Compose com PostgreSQL e perfis `local` / `docker`
- Pasta `infra/docker` para init do banco; Compose na raiz
- Enum `TipoFuncionario` com segregação de funções (SoD) e permissões granulares
- JWT com claims (`vinculo`, `situacaoCobranca`, tipo de acesso)
- Auditoria de ações críticas, Logback, Spring Actuator
- Domínio **Instituicao** (migração de nomenclatura Academia) + aliases `/academia` no backend
- Certificados com templates em `classpath` e saída em `backend/certificados/`
- UTF-8 em API e conexão PostgreSQL (perfil local)

### Acadêmico e portal
- Portal do aluno: dados, turmas, mensalidades, senha, **Minha programação**
- **CRUD Minha programação** pela instituição (`/instituicao/{id}/programacao`)
- **Grade horária** (turmas + itens), cadastro de **salas** e **detecção de conflitos** de horário/sala
- Turmas com horário início/fim, dias da semana (multiselect) e sala
- Telas **Consultar turmas** vs **Cadastrar turma** (formulário separado da lista)
- Matrícula com vínculo automático à instituição e senha inicial = 6 primeiros dígitos do CPF
- CPF único global (`tb_aluno`) e matrícula idempotente
- Seeds de cenários de teste — ver [CENARIOS_TESTE.md](./CENARIOS_TESTE.md)

### Financeiro e cobrança
- Plano SaaS da instituição (teste, mensal, trimestral, semestral, anual)
- **Tolerância de 5 dias** após vencimento (instituição e mensalidade do aluno)
- Dashboard administrativo e financeiro enxutos

### Autenticação (base)
- Login CPF + vínculo instituição, listagem de vínculos, telas de cadastro e solicitar acesso
- Recuperação de senha (API registra solicitação; **sem e-mail**)
- Guards de plano e cobrança no frontend

---

## Entregue — operador master da plataforma (jun/2026)

Objetivo desta fase: o **OPERADOR_PLATAFORMA** gerencia instituições, planos SaaS, visão financeira global e consultas cross-tenant.

### Identidade e acesso
- Master via `APP_MASTER_CPF` / `APP_MASTER_PASSWORD` (vínculo **Plataforma — Operação master**, ID `0`)
- Sub-masters delegados pelo master raiz (`permitirGerenciarFuncoes`)
- Menus **Consultar / Nova / Ativar instituição** restritos ao operador master
- Login com listagem de vínculos; instituições inativas não selecionáveis

### Instituições e plano SaaS
- **Nova instituição**: cadastro sem plano (cadastro inativo); plano na **ativação**
- Trial **uma vez** por instituição (`trialUtilizado`)
- **Consultar / Ativar instituições**, plano, status financeiro, administrador
- Data de vigência em **dd/MM/yyyy** na UI

### Financeiro plataforma
- Dashboard financeiro master, **Planos expirados**, **Pagamentos pendentes**

### Alunos (visão master)
- Consulta CPF com matrículas em **todas** as instituições
- Mensalidade e vencimento **por instituição** (`tb_matricula_instituicao`)

### Turmas (visão master)
- **Consultar turmas** com filtros: instituição, professor, dias

### Cenários validados (master)
| Menu | Status | Lacunas conhecidas |
|------|--------|-------------------|
| Instituições / plano SaaS | OK | Renovação automática por gateway (backlog) |
| Financeiro plataforma | OK | Checkout PIX/cartão (backlog) |
| Consultar alunos (cross-tenant) | OK | — |
| Consultar turmas (filtros) | OK | — |
| Matricular aluno (master) | OK | — |

---

## Entregue — perfil Professor (jul/2026)

Objetivo: experiência pedagógica do **PROFESSOR** vinculado à instituição, com escopo por turma e LGPD.

### Menus visíveis
| Seção | Itens |
|-------|-------|
| **Geral** | Início, Meu cadastro |
| **Área do professor** | Minhas turmas, Presença, Gerar certificados |
| **Acadêmico** | Consultar alunos, Programação e grade |

**Oculto:** Consultar turmas (instituição), Cadastrar turma, Matricular aluno, Financeiro, Administrativo.

### Minhas turmas
- Tela unificada (lista + detalhe): modalidade, sala, horário, dias, total de alunos
- Adicionar aluno por CPF (valida matrícula na instituição)
- Remover aluno da turma (sem desmatricular da instituição)
- API: `turma:gerenciar-alunos`, escopo por CPF do professor

### Presença
- Planilha mensal (P/F/J/A) por dias de aula da turma
- Salvar em lote + **PDF** (OpenPDF)
- `DiaSemanaUtil` aceita dias curtos (`Terça`, `Quinta`) e por extenso

### Gerar certificados
- Professor = usuário logado (sem dropdown)
- TXT por envio com alunos, faixas e quantidades; download automático
- Arquivo novo por data/hora (envios parciais preservados)

### Consultar alunos (professor)
- Somente leitura; qualquer aluno **matriculado na instituição** (opção B)
- Dados mascarados (CPF, telefone, e-mail, RG); sem aba financeira
- Endereço JSON parseado corretamente nos campos

### Programação e grade
- Professor: criar/editar/excluir **itens** (`programacao:gerenciar-itens`)
- Grade e **salas** somente leitura (sem cadastrar/excluir sala)

### Permissões novas
- `turma:gerenciar-alunos`, `programacao:gerenciar-itens`

### Testes backend (BDD)
- Padrão `@DisplayName("Dado … Quando … Então …")` e métodos `deveRealizarTalCoisa`
- Cobertura: escopo professor, turmas, presença, consulta LGPD, permissões, utilitários

### Cenários validados (professor)
| Menu | Status | Lacunas conhecidas |
|------|--------|-------------------|
| Minhas turmas | OK | — |
| Presença + PDF | OK | QR code (backlog) |
| Gerar certificados | OK | — |
| Consultar alunos | OK | Log de auditoria LGPD (backlog) |
| Programação (itens) | OK | — |
| Programação (salas) | Leitura | — |

**Login teste:** CPF `61482582007` / senha `123` — ver [USUARIOS_TESTE.md](./USUARIOS_TESTE.md). **Re-login** após deploy para carregar novas permissões no JWT.

---

## Próxima fase — perfis institucionais (ago/2026)

Prioridade sugerida após Professor e Master estáveis:

| # | Perfil / área | Itens |
|---|---------------|-------|
| 1 | **DIRETOR / ADMIN** | Revisão de menus vs permissões (SoD) |
| 2 | **RECEPÇÃO / RH** | Matrícula, ativação de cadastros, consultas |
| 3 | **FINANCEIRO** | Mensalidades, inadimplência, integração pagamento |
| 4 | **Portal aluno** | Pagamento online, notificações |
| 5 | **LGPD** | Consentimento, exportação, auditoria de consulta PII |
| 6 | **Infra** | Backup PostgreSQL, e-mail (SMTP), rate limiting |

---

## Pendente (backlog técnico)

1. **Pagamentos** — checkout (PIX/cartão), webhook
2. **Login/cadastro** — recuperação com e-mail real, MFA
3. **Grade** — filtro por sala, exportação PDF institucional
4. **Presença** — QR/code, consolidação multi-envio
5. **Limpeza de dados** — `MIGRACAO_DEDUP_ALUNO_CPF.sql`
6. **Backup automatizado** do PostgreSQL

---

## Como validar localmente

1. `docker compose up postgres -d` (porta **5435** no host)
2. Migrações SQL na ordem, se o banco já existia — ver [README.md](./README.md)
3. Backend perfil `local` ou `docker compose up -d --build`
4. Roteiros: [CENARIOS_TESTE.md](./CENARIOS_TESTE.md) e [USUARIOS_TESTE.md](./USUARIOS_TESTE.md)

**Master:** CPF `00000000191`, senha `Master@2024!`, vínculo Plataforma (`0`).  
**Professor:** CPF `61482582007`, senha `123`, Instituição Master.
