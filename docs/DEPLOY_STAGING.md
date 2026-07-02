# Deploy staging — Turma360

Ambiente de homologação local ou em VPS usando Docker Compose, sem túnel público.

## Pré-requisitos

- Docker e Docker Compose
- Arquivo `.env` (copie de `.env.example`)
- CI verde em `master` / `main`

## Subir staging

```bash
cp .env.example .env
# Ajuste JWT_SECRET, POSTGRES_PASSWORD, APP_MASTER_*

docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

| Serviço | URL local |
|---------|-----------|
| Frontend | http://localhost:80 |
| API | http://localhost:8000/srv-gerenciaracademia |
| Health | http://localhost:8000/srv-gerenciaracademia/actuator/health |

**Windows:** `subir-prod.bat` (stack com overlay de produção).

## Volumes persistentes

| Volume | Conteúdo |
|--------|----------|
| `postgres_data` | Banco PostgreSQL |
| `certificados_data` | Certificados gerados |
| `remuneracao_docs_data` | PDFs de holerite/recibo |

## Migração manual (perfil `prod` com `ddl-auto=validate`)

Antes do primeiro deploy em produção após atualização de holerite PDF:

```bash
psql -U academia_app -d gerenciamento_academia -f infra/sql/V001_documento_remuneracao_pdf.sql
```

## Pipeline CD (GitHub Actions)

O workflow [`.github/workflows/cd-staging.yml`](../.github/workflows/cd-staging.yml) executa após o CI:

1. Build das imagens `backend` e `frontend`
2. Validação de que o Compose sobe (smoke opcional em runner)

Publicação em registry (GHCR/ECR) fica para quando houver servidor de staging fixo.

## Checklist pós-deploy

- [ ] Login em `/entrar`
- [ ] Landing em `/`
- [ ] Upload de holerite PDF (Gestão de equipe)
- [ ] Download em Meu holerite
- [ ] Backup: `backup-banco.bat` ou `scripts/backup-postgres.sh`
