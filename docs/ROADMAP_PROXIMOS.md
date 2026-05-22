# Próximas entregas

## Concluído nesta fase (maio/2026)

### Backend
- Domínio **Instituicao** + `GerenciarInstituicaoController` e migração SQL
- Programação do aluno, salas, grade horária e validação de conflitos
- Cobrança: `SituacaoCobranca`, tolerância 5 dias, alerta/bloqueio no login
- `ServicoVinculoAlunoInstituicao`, portal com senha derivada do CPF
- CPF único e matrícula sem duplicar registro; consulta/listagem por instituição
- Ajustes JPA: `hashCode`/`equals` em relações ManyToMany, lista `dias` mutável em turmas
- Dashboard resumo (colaboradores ativos + aguardando ativação)
- Inicializadores: salas, cenários de teste, programação do aluno teste, vínculo turma portal

### Frontend
- Tela **Programação e grade** (`/arealogada/programacao`)
- Portal: **Minha programação**, **Alterar senha**, cards por tipo de item
- **Consultar turmas** / **Cadastrar turma** (rotas e menu separados)
- **Consultar alunos** (correção de rota `/alunos` vs portal `/aluno/*`)
- Matrícula com feedback de senha inicial; dashboards admin/financeiro enxutos
- `PlanoInstituicaoGuard`, interceptor de cobrança no login, menu por permissões

### Documentação e banco
- [CENARIOS_TESTE.md](./CENARIOS_TESTE.md), [USUARIOS_TESTE.md](./USUARIOS_TESTE.md) atualizado
- Scripts: `MIGRACAO_ACADEMIA_INSTITUICAO.sql`, `MIGRACAO_PROGRAMACAO_SCHEMA.sql`, `SEED_CENARIOS_TESTE.sql`, `MIGRACAO_DEDUP_ALUNO_CPF.sql`

## Pendente (prioridade sugerida)

1. **Pagamentos** — gateway PIX/cartão (mensalidade + plano instituição)
2. **JWT após renovar plano** — evitar logout manual após ativar assinatura
3. **E-mail** — recuperação de senha (hoje só registra solicitação)
4. **Grade** — filtro por sala, exportação PDF
5. **Presença** — chamada por turma/QR
6. **Limpeza de dados** — rodar `MIGRACAO_DEDUP_ALUNO_CPF.sql` em ambientes que matricularam CPF duplicado antes da correção

## Como validar localmente

1. `docker compose up postgres -d` (porta 5435)
2. Migrações SQL na ordem, se o banco já existia (ver arquivos em `docs/`)
3. Backend perfil `local`; frontend `npm run dev`
4. Roteiro: [CENARIOS_TESTE.md](./CENARIOS_TESTE.md)
