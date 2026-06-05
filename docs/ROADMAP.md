# Roadmap — EduGestão Inteligente

Documento único de planejamento. Histórico na seção **Entregue**; prioridade atual em **Fase atual — Perfil Professor**.

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

Objetivo desta fase: o **OPERADOR_PLATAFORMA** gerencia instituições, planos SaaS, visão financeira global e consultas cross-tenant sem depender do perfil DIRETOR da instituição.

### Identidade e acesso
- Master via `APP_MASTER_CPF` / `APP_MASTER_PASSWORD` (vínculo **Plataforma — Operação master**, ID `0`)
- Sub-masters delegados pelo master raiz (`permitirGerenciarFuncoes`)
- Menus **Consultar / Nova / Ativar instituição** restritos ao operador master
- Login com listagem de vínculos; instituições inativas não selecionáveis

### Instituições e plano SaaS
- **Nova instituição**: cadastro de dados básicos **sem** plano (cadastro inativo)
- **Ativar cadastro**: plano obrigatório (trial 7 dias, mensal, semestral, anual); trial **uma vez** por instituição (`trialUtilizado`)
- **Consultar instituições** e **Ativar / desativar**: plano, status financeiro manual, administrador e ativação/desativação
- Data de vigência do plano em formato **dd/MM/yyyy** na UI
- Renovação/alteração de plano com assinatura em `tb_assinatura_plataforma`

### Financeiro plataforma
- Dashboard financeiro master: planos vencidos, pagamentos pendentes, destaques
- Páginas **Planos expirados** e **Pagamentos pendentes**
- Contador de planos vencidos alinhado à regra `!assinatura.isVigente()`

### Alunos (visão master)
- **Consultar alunos** por CPF com matrículas em **todas** as instituições
- **Mensalidade e dia de vencimento por instituição** (`tb_matricula_instituicao`)
- Máscara **R$** em matrícula e consulta; cobrança/login usam a instituição do vínculo
- Matrícula master com busca de instituição por CNPJ

### Turmas (visão master)
- **Consultar turmas** com filtros combináveis: instituição (master), professor e dias de aula
- Escopo automático por instituição para usuários não master

### Colaboradores e outros
- Pré-cadastro / consulta de funcionários com cargo em dropdown
- Política de senha forte (cadastro e alteração)
- Correções: consulta aluno CPF (SQL DISTINCT), login bloqueado sem assinatura vigente

---

## Fase atual — perfil Professor (jul/2026)

Próximo foco de desenvolvimento. O master está funcional para operação da plataforma; agora refinamos a experiência do **PROFESSOR** vinculado à instituição.

| # | Item | Backend | Frontend | Notas |
|---|------|---------|----------|-------|
| 1 | **Minhas turmas** — listagem completa (dias, alunos, sala) | enriquecer `TurmaResumoDto` / endpoint professor | `MinhasTurmas.tsx` | hoje faltam campos no DTO |
| 2 | **Chamada / presença** por turma e data | entidade presença, API registrar | tela simples de marcação | MVP sem QR |
| 3 | **Consulta de alunos** da turma (somente leitura) | endpoint escopo professor + turma | lista na turma | sem editar mensalidade |
| 4 | **Programação** — visão do professor (itens + conflitos) | reutilizar `ServicoProgramacaoAluno` / grade | calendário ou lista semanal | |
| 5 | **Permissões** — revisar menu e guards só com `turma:consultar` / leitura | `TipoFuncionario.PROFESSOR` | `menuConfig.ts`, rotas | |
| 6 | Testes e roteiro | testes de serviço | [CENARIOS_TESTE.md](./CENARIOS_TESTE.md) | CPF professor nos seeds |

---

## Pendente (após fase Professor)

1. **Pagamentos** — checkout (PIX/cartão), e-mail instituição/admin, webhook para status financeiro
2. **Login/cadastro** — recuperação com e-mail real, rate limiting, MFA (ver itens 1–6 da fase login abaixo)
3. **Grade** — filtro por sala, exportação PDF
4. **Limpeza de dados** — `MIGRACAO_DEDUP_ALUNO_CPF.sql` em bancos legados
5. **Backup automatizado** do PostgreSQL
6. **LGPD** — consentimento e exportação de dados

### Fase login e infra (backlog próximo)
- Fluxo unificado login / cadastro / esqueci senha
- Recuperação de senha com e-mail (SMTP)
- JWT atualizado após renovar plano
- Rate limiting em endpoints públicos

---

## Backlog (fases futuras)

### Acadêmico
- [ ] Presença por QR/code
- [ ] Boletim e histórico escolar
- [ ] Comunicados para responsáveis (e-mail/SMS)
- [ ] Biblioteca de materiais (LMS leve)

### Financeiro
- [ ] Gateway de pagamento
- [ ] Descontos automatizados
- [ ] Nota fiscal / recibo
- [ ] DRE e fluxo de caixa consolidado

### RH e operação
- [ ] Folha de ponto
- [ ] Férias e substituições
- [ ] Avaliação de desempenho
- [ ] Contratos e documentos

### Comercial / multi-unidade
- [ ] Multi-tenant (franquias)
- [ ] CRM de leads
- [ ] App mobile

---

## Como validar localmente

1. `docker compose up postgres -d` (porta **5435** no host)
2. Migrações SQL na ordem, se o banco já existia — ver [README.md](./README.md)
3. Backend perfil `local` ou `docker compose up -d --build`
4. Roteiro master: [CENARIOS_TESTE.md](./CENARIOS_TESTE.md) e [USUARIOS_TESTE.md](./USUARIOS_TESTE.md)

**Master:** CPF `00000000191`, senha `Master@2024!`, vínculo Plataforma (`0`).
