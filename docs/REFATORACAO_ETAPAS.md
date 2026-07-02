# Plano de refatoração Turma360 — execução por etapas

Roadmap operacional da refatoração geral. Cada etapa tem **prompt de execução** para agentes ou desenvolvedores retomarem sem atropelar dependências.

## Status geral

| Etapa | Tema | Status |
|-------|------|--------|
| A | Marca Turma360 (nome, pacotes onda B) | Parcial — UI/docs ok; pacotes Java pendentes |
| B | Benchmark mercado | Documentado — ver [BENCHMARK_MERCADO.md](./BENCHMARK_MERCADO.md) |
| C | Site institucional + login SaaS | Em andamento — landing redesenhada; `/entrar` único |
| D | Design system + responsividade app | Pendente — tokens base ok; telas internas |
| E | Infra DevOps SaaS | Parcial — compose VPS/staging; RabbitMQ futuro |
| F | Escopo produto (corte ERP) | Em andamento — rotas legadas removidas da UI |
| G | Docs, scripts, limpeza raiz | Em andamento |

## Arquitetura SaaS — login (decisão)

**Modelo atual (correto para MVP):** um único portal `/entrar`. O usuário informa CPF → o sistema lista instituições vinculadas → escolhe o vínculo → JWT com `vinculo` (tenant).

Não usar subdomínio por instituição nesta fase (`escola-a.turma360.com.br`). Isso exige DNS wildcard, certificados e onboarding mais pesado. Evoluir para slug opcional (`/entrar/instituicao-slug`) só após domínio e primeiros clientes.

```mermaid
flowchart LR
  Home[Site turma360.com.br] --> Entrar[/entrar]
  Entrar --> CPF[CPF + senha]
  CPF --> Lista[Lista de instituições do usuário]
  Lista --> App[Painel arealogada com tenant no token]
```

---

## Etapa A — Marca (onda A concluída, onda B adiada)

**Prompt:** Confirmar domínio `turma360.com.br` no Registro.br. Manter `gerenciamentoDeAcademia` em pacotes Java até domínio ativo. Garantir `APP_NAME`, scripts `.bat`, compose e docs usam Turma360.

**Feito:** `branding.ts`, LogoMark CSS, scripts renomeados, landing `/`.

**Pendente onda B:** rename pacotes Maven, containers `academia-*` → `turma360-*`, repo GitHub.

---

## Etapa B — Benchmark

**Prompt:** Ler sites [iScholar](https://www.ischolar.com.br/), [Sponte](https://www.sponte.com.br/gestao-pedagogica), [Gestão Educação](https://gestaoeducacao.com.br/), [EduGestão](https://edugestao.com.br/), [Cogna](https://www.kroton.com.br/). Atualizar matriz em `BENCHMARK_MERCADO.md`: o que copiar (landing, portal aluno, cobrança escolar) vs. o que não copiar (ERP, folha completa, BNCC enterprise).

---

## Etapa C — Site institucional

**Prompt:** Redesenhar `MarketingHome.tsx` + `marketing.css` inspirado em iScholar/Sponte: hero com gradiente, segmentos (esportes, idiomas, cursos livres), grid de recursos, bloco “como funciona o acesso”, prova social, CTA “Acessar minha conta” → `/entrar`. Mobile-first. Não misturar com painel logado.

**Critério de pronto:** Lighthouse mobile aceitável; link Entrar visível no header sticky; seção explicando login único + escolha de instituição.

---

## Etapa D — Design system + app responsivo

**Prompt:** Estender `tokens.css` e `layout.css`. Sidebar colapsável já existe — revisar breakpoints 600/900/1200px. Padronizar cards, tabelas responsivas (`overflow-x: auto`), formulários em coluna no mobile. Aplicar em: `TelaInicial`, listagens alunos/turmas, `GestaoProgramacao`, portal aluno.

**Não fazer nesta etapa:** reescrever styled-components legados de uma vez; ir tela a tela.

---

## Etapa E — DevOps SaaS

**Prompt:** Manter stack leve: Postgres + API + nginx + cloudflared (dev). Produção: `docker-compose.vps-app.yml` (app) + Postgres dedicado. Variáveis por tenant não no compose — tenant no JWT. Preparar fila RabbitMQ (integrações Asaas/webhooks) sem subir container até etapa de integrações.

Ver [DEPLOY_VPS.md](./DEPLOY_VPS.md), [INTEGRACOES.md](./INTEGRACOES.md).

---

## Etapa F — Corte de escopo ERP

**Prompt:** Remover da **navegação e rotas** (não expandir backend):

- Folha de pagamento interna (`/financeiro/folha-pagamento`)
- Conciliação bancária (`/financeiro/conciliacao`)
- Fechamento de mês/caixa (`/financeiro/fechamento-mes`)

**Manter:**

- Mensalidades e inadimplência (cobrança escolar)
- Gestão de equipe: ponto, férias, holerite PDF anexo
- Financeiro **plataforma** (master): planos SaaS, pagamentos pendentes

Atualizar [ESCOPO_PRODUTO.md](./ESCOPO_PRODUTO.md) se surgir nova decisão.

---

## Etapa G — Limpeza raiz e scripts

**Prompt:** Manter na raiz apenas: `README.md`, `docker-compose*.yml`, `.env.example`, `subir.bat` (atalho). Demais `.bat` documentados em `scripts/README.md`. Remover duplicatas e referências EduGestão/academia na documentação.

---

## Integrações e mensageria (pós-refatoração visual)

1. **Asaas** — cobrança mensalidade + plano instituição (modo local já simula)
2. **Brevo** — e-mail transacional + recuperação de senha com link real
3. **Twilio** — WhatsApp régua de cobrança
4. **RabbitMQ** — fila webhooks e notificações (container + consumers)

Ordem sugerida: Brevo (senha) → Asaas sandbox → Twilio → RabbitMQ.

---

## Como retomar no Cursor

```
Continue a refatoração Turma360 seguindo docs/REFATORACAO_ETAPAS.md.
Execute apenas a Etapa [X]. Não reinicie Docker sem pedido.
```
