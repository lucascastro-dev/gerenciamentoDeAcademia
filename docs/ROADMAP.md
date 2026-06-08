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

## Entregue — perfil Administrativo (jun/2026)

Objetivo: o **ADMINISTRADOR** da instituição com visão operacional completa (exceto funções exclusivas do operador master da plataforma).

### Menus visíveis
| Seção | Itens |
|-------|-------|
| **Geral** | Início, Meu cadastro, Dashboard administrativo |
| **Financeiro** | Dashboard financeiro, Mensalidades, Inadimplência |
| **Acadêmico** | Consultar alunos, Consultar turmas, Cadastrar turma, Matricular aluno, Programação e grade |
| **Administrativo** | Funcionários, Ativar cadastros, Plano da instituição |

**Oculto:** Área do professor, Consultar/nova/ativar instituição (master), Auditoria global.

### Permissões
- Financeiro institucional: `financeiro:visualizar`, `financeiro:cobranca`, `financeiro:relatorio`
- Demais permissões acadêmicas e administrativas já existentes (funcionários, turmas, alunos, programação, plano)
- Rota do dashboard protegida por `dashboard:visualizar`

### Dashboard administrativo (instituição)
- Indicadores **escopados à instituição** logada (alunos, colaboradores ativos, turmas)
- Removido card **Aguardando ativação** (ativação por CPF na instituição não é mais o fluxo principal)

### Consultar turmas
- Listagem por instituição via `TurmaListagemDto` (sem lazy load na API)
- Busca por nome da turma, filtro por professor, paginação 5/15/25/50/100
- Detalhe em **tela separada** (padrão alunos/funcionários): editar dados, vincular professor, excluir

### Pré-cadastro colaborador
- Campo **e-mail** obrigatório com validação (`PoliticaEmail` backend + `emailPolicy` frontend)

### Consultar alunos (professor) — correção
- Listagem unifica alunos por **matrícula** e **turma** na instituição
- CPF completo na listagem para uso em Minhas turmas

### Cenários validados (administrador)
| Menu | Status | Lacunas conhecidas |
|------|--------|-------------------|
| Dashboard instituição | OK | — |
| Financeiro (mensalidades / inadimplência) | OK | Gateway PIX/cartão (backlog) |
| Consultar alunos | OK | — |
| Consultar turmas | OK | — |
| Funcionários / ativar cadastros | OK | — |

**Re-login** após deploy para carregar permissões financeiras no JWT.

---

## Próxima fase — perfis institucionais (ago/2026)

Prioridade sugerida após Administrador estável:

| # | Perfil / área | Itens |
|---|---------------|-------|
| 1 | **DIRETOR** | Menus e SoD alinhados ao papel estratégico |
| 2 | **RECEPÇÃO / RH** | Refino de matrícula, ativação e fluxos de cadastro |
| 3 | **FINANCEIRO** | Gateway de pagamento, relatórios avançados |
| 4 | **Portal aluno** | Pagamento online, notificações |
| 5 | **LGPD** | Consentimento, exportação, auditoria de consulta PII |
| 6 | **Infra** | Backup PostgreSQL, e-mail (SMTP), rate limiting |
| 7 | **Cantina** | Ver seção dedicada no roadmap |

---

## Pendente (backlog técnico)

1. **Pagamentos** — checkout (PIX/cartão), webhook
2. **Login/cadastro** — recuperação com e-mail real, MFA
3. **Grade** — filtro por sala, exportação PDF institucional
4. **Presença** — QR/code, consolidação multi-envio
5. **Limpeza de dados** — `MIGRACAO_DEDUP_ALUNO_CPF.sql`
6. **Backup automatizado** do PostgreSQL

---

## Implementações futuras — Módulo Cantina

Solicitação de cliente para **escolas infantis** e demais instituições com cantina/lanchonete. Compra antecipada pelo portal (aluno ou responsável), sem presença física no momento do pedido.

### Objetivo

Permitir **reserva/compra virtual** de itens da cantina, com fluxo de pagamento manual inicial (comprovante PIX) e evolução para **PIX automático** integrado ao mesmo gateway previsto para planos SaaS e mensalidades.

### Menu principal: **Cantina**

Visível conforme perfil (detalhe de permissões na implementação).

| Perfil | Submenus sugeridos | Função |
|--------|-------------------|--------|
| **Aluno / responsável** (portal) | **Cardápio virtual**, **Meus pedidos** | Montar carrinho, pagar (manual → PIX API), acompanhar status |
| **Administrador / cantina** (instituição) | **Cardápio e estoque**, **Gestão de pedidos** | Cadastro de itens, estoque exibido ao aluno, fila operacional |

*Nomes dos submenus podem ser refinados na UI (ex.: “Cardápio”, “Pedidos da cantina”).*

### Portal do aluno / responsável

**Cardápio virtual**
- Lista de itens disponíveis (origem: estoque da instituição)
- Indicadores: disponível / esgotado / limite por dia (a definir)
- Carrinho com quantidades e valor total
- Checkout **fase 1 (manual):** instruções PIX da instituição + upload de comprovante no próprio pedido
- Checkout **fase 2 (API):** geração de cobrança PIX e confirmação automática (mesma linha de integração de pagamentos do backlog)

**Meus pedidos**
- Histórico e pedidos em andamento
- Status visível ao cliente (ver fluxo abaixo)

**Quem compra**
- Aluno logado no portal (CPF do aluno)
- Responsável vinculado ao aluno (quando aplicável ao cadastro existente)

### Administrador da instituição

**Cardápio e estoque**
- CRUD de itens (nome, descrição, preço, categoria, foto opcional)
- Controle de estoque / disponibilidade (quantidade ou flag ativo)
- Itens publicados refletem no cardápio virtual do aluno

**Gestão de pedidos**
- Fila com filtros por status e data
- Ações: **aprovar pagamento** (comprovante manual), **em preparo**, **pronto para retirada**, **entregue**, **cancelado**
- Visão do comprovante anexado (fase manual)
- Notificação in-app ou e-mail (backlog de notificações)

### Fluxo de status do pedido (proposta)

```
CARRINHO → AGUARDANDO_PAGAMENTO → PAGAMENTO_EM_ANALISE → PAGO
    → EM_PREPARO → PRONTO → ENTREGUE
         ↘ CANCELADO / PAGAMENTO_RECUSADO
```

| Status | Quem altera | Observação |
|--------|-------------|------------|
| Aguardando pagamento | Sistema / aluno | Pedido criado; PIX manual ou aguardando API |
| Pagamento em análise | Admin | Comprovante enviado; conferência manual |
| Pago | Admin ou webhook PIX | Libera preparo |
| Em preparo / Pronto / Entregue | Admin / operador cantina | Operação da cozinha/balcão |
| Cancelado / Recusado | Admin ou timeout | Estorno de estoque reservado |

### Cenários e regras de negócio (a detalhar na implementação)

1. **Escola infantil — responsável:** menor sem autonomia de compra; responsável usa portal com vínculo ao aluno.
2. **Aluno maior / fundamental:** compra com CPF próprio se política da instituição permitir.
3. **Estoque:** reserva ao confirmar pedido; baixa definitiva ao marcar **Entregue** (ou ao pagar — decisão na spec).
4. **Horário de pedido:** janela para pedido antecipado (ex.: até 20h do dia anterior) — configurável por instituição.
5. **Retirada:** data/turno (manhã, intervalo, almoço) selecionável no checkout.
6. **Multi-instituicao:** aluno matriculado em mais de uma unidade escolhe a instituição/cantina no contexto do vínculo logado.
7. **Master:** sem operação de cantina cross-tenant na v1; escopo sempre pela instituição do vínculo.
8. **LGPD:** comprovantes e dados de pedido com retenção e acesso auditável.

### Modelo de dados (rascunho)

- `tb_cantina_item` — item do cardápio (instituição, preço, estoque, ativo)
- `tb_cantina_pedido` — pedido (aluno, instituição, total, status, turno retirada)
- `tb_cantina_pedido_item` — linhas do pedido
- `tb_cantina_pagamento` — comprovante manual (arquivo, status análise) ou referência gateway PIX

### APIs (rascunho)

| Área | Endpoints (prefixo sugerido `/cantina`) |
|------|----------------------------------------|
| Portal | `GET /itens`, `POST /pedidos`, `POST /pedidos/{id}/comprovante`, `GET /pedidos/meus` |
| Admin | `CRUD /itens`, `GET /pedidos`, `PATCH /pedidos/{id}/status`, `PATCH /pedidos/{id}/aprovar-pagamento` |

### Permissões (rascunho)

- `cantina:cardapio-consultar` — aluno/responsável
- `cantina:pedido-criar`, `cantina:pedido-consultar-proprio`
- `cantina:item-gerenciar`, `cantina:pedido-gerenciar`, `cantina:pagamento-aprovar`

### Fases de entrega sugeridas

| Fase | Escopo |
|------|--------|
| **1** | Cardápio + estoque (admin) + cardápio virtual + carrinho + pedido com PIX manual e comprovante |
| **2** | Meus pedidos + fluxo de status completo (preparo → entrega) |
| **3** | PIX automático (API) alinhado à integração de mensalidades/planos |
| **4** | Relatórios, limites por turma/série, notificações |

### Dependências

- Módulo de **upload de arquivos** (comprovante) com armazenamento seguro
- Integração de **pagamentos** (backlog item 1)
- Definição de política **aluno vs responsável** por faixa etária (config instituição)

---

## Como validar localmente

1. `docker compose up postgres -d` (porta **5435** no host)
2. Migrações SQL na ordem, se o banco já existia — ver [README.md](./README.md)
3. Backend perfil `local` ou `docker compose up -d --build`
4. Roteiros: [CENARIOS_TESTE.md](./CENARIOS_TESTE.md) e [USUARIOS_TESTE.md](./USUARIOS_TESTE.md)

**Master:** CPF `00000000191`, senha `Master@2024!`, vínculo Plataforma (`0`).  
**Professor:** CPF `61482582007`, senha `123`, Instituição Master.
