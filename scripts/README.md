# Scripts — Turma360

Scripts de operação local (Windows/Linux). Na **raiz** do repositório fica apenas `subir.bat` (stack completa). Demais atalhos Windows estão em `scripts/windows/`.

## Subida Docker (stack completa)

| Script | Descrição |
|--------|-----------|
| `subir.bat` (raiz) | Sobe postgres + backend + frontend + túnel. Use `subir.bat build` para rebuild. |
| `scripts/subir-servico.ps1` | Motor PowerShell: `-Servico postgres\|backend\|frontend\|tunnel\|todos` |
| `scripts/subir-docker.sh` | Equivalente Linux/macOS |

### Serviços individuais (`scripts/windows/`)

| Script | Serviço |
|--------|---------|
| `subir-postgres.bat` | Banco PostgreSQL |
| `subir-backend.bat` | API Spring Boot (`build` para rebuild) |
| `subir-frontend.bat` | Nginx + SPA (`build` para rebuild) |
| `subir-tunel.bat` | Cloudflared (URL em `URL_PUBLICA.txt`) |
| `subir-prod.bat` | Produção (sem túnel, overlay `infra/docker/docker-compose.prod.yml`) |

## Backup e banco

| Script | Descrição |
|--------|-----------|
| `backup-banco.bat` | Backup manual PostgreSQL → `backups/postgres/` |
| `restaurar-banco.bat` | Restaura dump |
| `agendar-backup-semanal.bat` | Tarefa agendada Windows |

## Utilitários

| Script | Descrição |
|--------|-----------|
| `scripts/configurar-portas.py` | Valida portas do `.env` + firewall Windows |
| `scripts/aguardar-url-publica.py` | Extrai URL do túnel cloudflared |
| `atualizar-url-publica.bat` | Atualiza `URL_PUBLICA.txt` sem subir stack |

## Docker Compose

| Arquivo | Uso |
|---------|-----|
| `docker-compose.yml` | Stack base (dev) |
| `infra/docker/docker-compose.prod.yml` | Overlay produção |
| `infra/docker/docker-compose.demo.yml` | Demo com seeds |
| `infra/docker/docker-compose.ngrok.yml` | Túnel ngrok fixo |
| `infra/docker/docker-compose.vps-app.yml` | App sem Postgres local |

Ver [DEPLOY_VPS.md](../docs/DEPLOY_VPS.md).
