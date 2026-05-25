# Deploy com Docker (local e testadores na rede)

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/Mac) ou Docker Engine + Compose (Linux)
- Porta **5173** livre no host (ou altere `APP_PORT` no `.env`)

## Subir na máquina de deploy

```bash
git pull
cp .env.example .env
# Edite .env: JWT_SECRET, senhas do master e POSTGRES_PASSWORD em ambiente exposto
docker compose up -d --build
```

**Windows (PowerShell):**

```powershell
.\scripts\subir-docker.ps1
```

Aguarde ~1–2 min na primeira vez (build do backend). Verifique:

```bash
docker compose ps
docker compose logs -f backend
```

| O quê | URL |
|--------|-----|
| **Aplicação (compartilhe este link)** | `http://SEU_IP:5173` |
| Só na máquina host | http://localhost:5173 |
| API direta (debug/Swagger) | http://localhost:8000/srv-gerenciaracademia/swagger-ui.html |

Descobrir IP (rede local):

- Windows: `ipconfig` → IPv4 (ex.: `192.168.1.42`)
- Linux/Mac: `hostname -I` ou `ip addr`

Testadores na **mesma Wi‑Fi/rede** abrem: **http://192.168.1.42:5173** (substitua pelo seu IP).

## Como funciona

- O **nginx** do container `frontend` encaminha `/srv-gerenciaracademia` para o `backend`.
- O frontend usa URL **relativa** da API — não precisa configurar IP no build.
- Credenciais de teste: [USUARIOS_TESTE.md](./USUARIOS_TESTE.md).

## Modo demo (só porta do app)

Esconde a API na porta 8000 do host; testadores usam apenas o frontend (API via proxy):

```bash
docker compose -f docker-compose.yml -f docker-compose.demo.yml up -d --build
```

Ou no Windows:

```powershell
.\scripts\subir-docker.ps1 -Demo
```

## Firewall (Windows)

Para outras pessoas na rede alcançarem sua máquina:

1. Painel de Controle → Firewall do Windows → Configurações avançadas
2. Regra de entrada → TCP → porta **5173** (e **8000** se não usar `-Demo`)

Ou PowerShell (administrador):

```powershell
New-NetFirewallRule -DisplayName "EduGestao App" -Direction Inbound -Protocol TCP -LocalPort 5173 -Action Allow
```

## Internet (fora da LAN)

Opções:

1. **Túnel** (ngrok, Cloudflare Tunnel) apontando para `localhost:5173` — uma URL pública basta (API já passa pelo nginx).
2. **VPS** com Docker: clone o repo, `docker compose up -d --build`, libere porta 5173 no security group.

Exemplo ngrok:

```bash
ngrok http 5173
```

Envie o link `https://xxxx.ngrok-free.app` aos testadores.

## Variáveis úteis (`.env`)

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `APP_PORT` | `5173` | Porta do app no host |
| `POSTGRES_PASSWORD` | `academia_dev_secret` | Senha do banco |
| `JWT_SECRET` | (exemplo) | **Troque** em demo pública |
| `APP_MASTER_PASSWORD` | `Master@2024!` | Senha do master |
| `VITE_API_URL` | `/srv-gerenciaracademia` | Só mude se rebuildar o frontend com URL absoluta |

## Problemas comuns

| Sintoma | Solução |
|---------|---------|
| `password authentication failed` | Volume antigo com senha diferente: `docker compose down -v` e subir de novo |
| Testador não abre o site | Firewall, IP errado ou não estão na mesma rede |
| Página abre mas login falha | `docker compose logs backend`; aguarde backend healthy |
| Porta em uso | Mude `APP_PORT=8080` no `.env` e `docker compose up -d --build` |

## Desenvolvimento local (sem rebuild Docker)

Backend: `mvn spring-boot:run` (perfil `local`).  
Frontend: crie `frontend/.env.local`:

```env
VITE_API_URL=http://localhost:8000/srv-gerenciaracademia
```

```bash
cd frontend && npm run dev
```

## Parar / limpar

```bash
docker compose down          # mantém dados do Postgres
docker compose down -v       # apaga volume do banco
```
