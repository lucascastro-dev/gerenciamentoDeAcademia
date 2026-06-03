# Roadmap — EduGestão Inteligente

Documento único de planejamento. Histórico da entrega de maio/2026 está na seção **Entregue**; prioridades atuais em **Fase atual** e **Pendente**.

## Entregue (base + evolução recente)

### Infra e plataforma
- Docker Compose com PostgreSQL e perfis `local` / `docker`
- Pasta `infra/docker` para init do banco; Compose na raiz
- Usuário master (DIRETOR) e perfis de teste na subida do container
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
- **Consultar alunos** com lista e edição por CPF (escopo da instituição)
- Matrícula com vínculo automático à instituição e senha inicial = 6 primeiros dígitos do CPF
- CPF único global (`tb_aluno`) e matrícula idempotente
- Seeds de cenários de teste — ver [CENARIOS_TESTE.md](./CENARIOS_TESTE.md)

### Financeiro e cobrança
- Plano SaaS da instituição (teste, mensal, trimestral, semestral, anual)
- **Tolerância de 5 dias** após vencimento (instituição e mensalidade do aluno)
- Dashboard administrativo e financeiro enxutos

### Autenticação (base já existente)
- Login CPF + vínculo instituição, listagem de vínculos, telas de cadastro e solicitar acesso
- Recuperação de senha (API registra solicitação; **sem e-mail**)
- Guards de plano e cobrança no frontend

---

## Fase atual — login, cadastro e infra (jun/2026)

Objetivo: endurecer autenticação e onboarding antes de novos módulos de negócio.

| # | Item | Backend | Frontend | Infra |
|---|------|---------|----------|-------|
| 1 | Fluxo unificado login / cadastro / esqueci senha (UX + mensagens) | revisar `LoginController`, `GerenciadorDeLogin` | `TelaLogin/*` | — |
| 2 | Recuperação de senha com **e-mail real** (SMTP ou provedor) | serviço de notificação, token temporário | `EsqueciSenha` | variáveis `.env`, secrets |
| 3 | Política de senha (complexidade, histórico) | validação no cadastro/alteração | feedback em formulários | — |
| 4 | JWT atualizado após **renovar plano** da instituição | refresh de claims sem logout | interceptor / re-login silencioso | — |
| 5 | Rate limiting no login e endpoints públicos | filtro ou gateway | — | documentar em `DEPLOY_DOCKER` |
| 6 | MFA (2FA) — desenho e POC | TOTP / backup codes | tela de configuração | — |
| 7 | Documentação e estrutura | — | — | `infra/`, `docs/ARQUITETURA.md` ✅ |

---

## Pendente (prioridade após fase atual)

1. **Pagamentos** — gateway PIX/cartão (mensalidade + plano instituição)
2. **Grade** — filtro por sala, exportação PDF
3. **Presença** — chamada por turma/QR
4. **Limpeza de dados** — `MIGRACAO_DEDUP_ALUNO_CPF.sql` em bancos legados
5. **Backup automatizado** do PostgreSQL
6. **LGPD** — consentimento e exportação de dados

---

## Backlog (fases futuras)

### Acadêmico
- [ ] Controle de presença por QR/code
- [ ] Boletim e histórico escolar
- [ ] Comunicados para responsáveis (e-mail/SMS)
- [ ] Biblioteca de materiais (LMS leve)

### Financeiro
- [ ] Gateway de pagamento (detalhamento)
- [ ] Plano de mensalidade e descontos automatizados
- [ ] Nota fiscal / recibo
- [ ] DRE e fluxo de caixa consolidado

### RH e operação
- [ ] Folha de ponto
- [ ] Férias e substituições de professores
- [ ] Avaliação de desempenho
- [ ] Contratos e documentos (upload)

### Comercial / multi-unidade
- [ ] Multi-tenant (várias unidades/franquias)
- [ ] CRM de leads (matrícula online)
- [ ] App mobile (React Native)

### Relatórios executivos
- [ ] Churn e retenção
- [ ] Ocupação de turmas
- [ ] Indicadores por modalidade

---

## Como validar localmente

1. `docker compose up postgres -d` (porta **5435** no host)
2. Migrações SQL na ordem, se o banco já existia — ver [README.md](./README.md)
3. Backend perfil `local`; frontend `npm run dev` com `frontend/.env.local`
4. Roteiro: [CENARIOS_TESTE.md](./CENARIOS_TESTE.md) e [USUARIOS_TESTE.md](./USUARIOS_TESTE.md)
