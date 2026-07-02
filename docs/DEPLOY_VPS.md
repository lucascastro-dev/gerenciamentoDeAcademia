# Deploy em VPS — Turma360

Arquitetura alvo: **duas VPS Linux** com Docker.

```mermaid
flowchart LR
  subgraph vps_app [VPS_Aplicacao]
    Nginx[Nginx_frontend]
    API[Spring_Boot]
    VolDocs[Volume_PDFs]
  end
  subgraph vps_db [VPS_Banco]
    PG[(PostgreSQL_16)]
  end
  Internet --> Nginx
  Nginx --> API
  API -->|TLS_5432| PG
  Asaas[Asaas_webhooks] --> API
  Brevo[Brevo_API] <-- API
  Twilio[Twilio_API] <-- API
```

## VPS 1 — Banco de dados

- Ubuntu 22.04+ ou Debian 12
- Docker apenas para Postgres **ou** Postgres nativo
- Firewall: porta **5432** só para IP da VPS app
- Backup diário (`pg_dump`) para storage externo

Exemplo mínimo Postgres (Docker na VPS DB):

```bash
docker run -d --name turma360-postgres \
  -e POSTGRES_DB=gerenciamento_academia \
  -e POSTGRES_USER=academia_app \
  -e POSTGRES_PASSWORD='***' \
  -v turma360_pg:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:16-alpine
```

## VPS 2 — Aplicação

- Clone do repositório + `.env` de produção
- **Sem** serviço `postgres` no Compose (use overlay abaixo)
- Nginx expõe 80/443; API na 8000 (interno)
- Volumes: `certificados_data`, `remuneracao_docs_data`

```bash
docker compose -f docker-compose.yml -f infra/docker/docker-compose.prod.yml -f infra/docker/docker-compose.vps-app.yml up -d --build
```

Variáveis na VPS app:

```env
APP_INTEGRACOES_MODO_LOCAL=false
BREVO_ENABLED=true
BREVO_API_KEY=...
ASAAS_ENABLED=true
ASAAS_API_KEY=...
ASAAS_WEBHOOK_TOKEN=...
DB_HOST=10.x.x.x
POSTGRES_PASSWORD=...
JWT_SECRET=...
```

## DNS e TLS

Sem domínio registrado ainda: use IP + certificado autoassinado ou Cloudflare Tunnel na VPS app.

Com domínio futuro (`turma360.com.br`): aponte A/AAAA para VPS app; Certbot no Nginx.

## Checklist pós-deploy

- [ ] `actuator/health` OK
- [ ] Login `/entrar`
- [ ] Webhook Asaas apontando para `https://seu-host/webhooks/asaas`
- [ ] Backup do Postgres na VPS DB
- [ ] `APP_SEED_DEMO=false` em produção

Ver também [DEPLOY_STAGING.md](./DEPLOY_STAGING.md) e [INTEGRACOES.md](./INTEGRACOES.md).
