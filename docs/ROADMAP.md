# Roadmap — EduGestão Inteligente

## Entregue (base + evolução recente)

### Infra e plataforma
- Docker Compose com PostgreSQL e perfis `local` / `docker`
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
- CPF único global (`tb_aluno`) e matrícula idempotente (reutiliza aluno existente em nova instituição)
- Seeds de cenários de teste (turmas, programação, conflitos na grade) — ver [CENARIOS_TESTE.md](./CENARIOS_TESTE.md)

### Financeiro e cobrança
- Plano SaaS da instituição (teste, mensal, trimestral, semestral, anual)
- **Tolerância de 5 dias** após vencimento (plano da instituição e mensalidade do aluno): alerta no login; bloqueio com orientação de SAC depois
- Dashboard administrativo (colaboradores ativos / aguardando ativação, turmas, etc.)
- Dashboard financeiro (sem lista duplicada de vencimentos — link para Mensalidades)

## Funcionalidades sugeridas (próximas fases)

### Acadêmico
- [ ] Controle de presença por QR/code
- [ ] Boletim e histórico escolar
- [ ] Comunicados para responsáveis (e-mail/SMS)
- [ ] Biblioteca de materiais (LMS leve)
- [ ] Grade: filtro por sala e exportação PDF/impressão

### Financeiro
- [ ] Gateway de pagamento (PIX/cartão) — mensalidade e plano instituição
- [ ] Plano de mensalidade e descontos automatizados
- [ ] Nota fiscal / recibo
- [ ] DRE e fluxo de caixa consolidado

### RH e operação
- [ ] Folha de ponto
- [ ] Férias e substituições de professores
- [ ] Avaliação de desempenho
- [ ] Contratos e documentos (upload)

### TI e segurança
- [ ] MFA (2FA) no login
- [ ] Política de senha e expiração (colaboradores)
- [ ] LGPD: consentimento e exportação de dados
- [ ] Backup automatizado do PostgreSQL
- [ ] Rate limiting e WAF
- [ ] Renovação de plano sem novo login (atualizar JWT após ativar)

### Comercial / multi-unidade
- [ ] Multi-tenant (várias unidades/franquias)
- [ ] CRM de leads (matrícula online)
- [ ] App mobile (React Native)

### Relatórios executivos
- [ ] Churn e retenção
- [ ] Ocupação de turmas
- [ ] Indicadores por modalidade
