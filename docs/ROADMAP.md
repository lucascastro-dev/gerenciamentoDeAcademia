# Roadmap — Sistema de Gestão Educacional

## Entregue nesta evolução

- Docker Compose com PostgreSQL e perfil `docker`
- Usuário master automático (DIRETOR) na subida do container
- Enum `TipoFuncionario` com segregação de funções (SoD)
- Permissões granulares + JWT com claims
- Auditoria de ações críticas (`tb_auditoria`)
- Logging estruturado (Logback) + Spring Actuator
- Dashboard resumo (API)
- Endpoints protegidos com `@PreAuthorize`

## Funcionalidades sugeridas (próximas fases)

### Acadêmico
- [ ] Grade horária e calendário letivo
- [ ] Controle de presença por QR/code
- [ ] Boletim e histórico escolar
- [ ] Comunicados para responsáveis (e-mail/SMS)
- [ ] Biblioteca de materiais (LMS leve)

### Financeiro
- [ ] Plano de mensalidade e descontos
- [ ] Gateway de pagamento (PIX/cartão)
- [ ] Inadimplência automatizada
- [ ] Nota fiscal / recibo
- [ ] DRE e fluxo de caixa consolidado

### RH e operação
- [ ] Folha de ponto
- [ ] Férias e substituições de professores
- [ ] Avaliação de desempenho
- [ ] Contratos e documentos (upload)

### TI e segurança
- [ ] MFA (2FA) no login
- [ ] Política de senha e expiração
- [ ] LGPD: consentimento e exportação de dados
- [ ] Backup automatizado do PostgreSQL
- [ ] Rate limiting e WAF

### Comercial / multi-unidade
- [ ] Multi-tenant (várias unidades/franquias)
- [ ] CRM de leads (matrícula online)
- [ ] Portal do aluno/responsável
- [ ] App mobile (React Native)

### Relatórios executivos
- [ ] Churn e retenção
- [ ] Ocupação de turmas
- [ ] Indicadores por modalidade
