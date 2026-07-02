# Deploy com Docker (rede local e internet)

## Erro `load metadata` / timeout no Docker Hub

Mensagens como `registry-1.docker.io: context deadline exceeded` indicam que o **Docker Hub** não responde na sua rede.

O projeto usa **mirror público da AWS (ECR)** para Node, Nginx, Postgres e Java. O `subir.bat` tenta ECR antes do Hub.

1. Abra o **Docker Desktop** e aguarde ficar em execução.
2. Execute **`subir.bat`** de novo.
3. Teste manual:
   ```bash
   docker pull public.ecr.aws/docker/library/node:20-alpine
   docker compose up -d --build
   ```
4. Se só o túnel (`cloudflared`) falhar, a app sobe em http://localhost:5173 sem URL pública.
5. VPN/proxy: configure em *Docker Desktop → Settings* ou teste outra rede (ex.: celular).

## Erro `backend build ... mvnw dependency:go-offline`

O passo antigo `dependency:go-offline` costuma falhar no Alpine (rede lenta, SSL ou download de plugins Maven).

O `backend/Dockerfile` atual usa `dependency:resolve` + `resolve-plugins`, que é mais estável.

1. Atualize o repositório (`git pull`) ou confira se o Dockerfile não contém mais `go-offline`.
2. Rebuild forçado:
   ```bash
   docker compose build backend --no-cache
   docker compose up -d
   ```
3. Se ainda falhar ao baixar dependências: teste internet/VPN e `docker compose build backend` com Docker Desktop em execução.

## Forma mais simples (Windows)

1. Instale [Docker Desktop](https://www.docker.com/products/docker-desktop/) e deixe rodando.
2. `copy .env.example .env` e ajuste senhas.
3. Execute **`subir.bat`** na raiz do projeto (ou `docker compose up -d --build`).
4. Copie o link **HTTPS** de `URL_PUBLICA.txt` ou `docker compose logs tunnel`.
5. Envie o link + [USUARIOS_TESTE.md](./USUARIOS_TESTE.md) aos testadores.

Passo a passo detalhado: **[PASSO_A_PASSO_DEPLOY.txt](../PASSO_A_PASSO_DEPLOY.txt)** na raiz do repositório.

## Teste externo (internet)

O serviço **`tunnel`** (Cloudflare Quick Tunnel) gera uma URL pública sem configurar roteador:

- `docker compose up -d --build` sobe app + túnel automaticamente.
- A URL muda a cada `docker compose down` / `up` (ex.: `https://xxx.trycloudflare.com`).
- Após reiniciar só o túnel, execute **`atualizar-url-publica.bat`** para atualizar `URL_PUBLICA.txt` (o script usa a URL **mais recente** dos logs, não a primeira da história).

API e frontend usam o **mesmo host** (nginx faz proxy) — funciona para quem acessa de qualquer lugar.

### URL fixa (ngrok)

1. Conta em https://ngrok.com → copie o authtoken.
2. No `.env`: `NGROK_AUTHTOKEN=...`
3. Suba com:

```bash
docker compose -f docker-compose.yml -f infra/docker/docker-compose.ngrok.yml up -d --build
```

## Scripts

| Arquivo | Função |
|---------|--------|
| `subir.bat` | Sobe stack completa (pull de imagens; **sem rebuild** por padrão). Use `subir.bat build` na 1ª vez ou após mudar código. |
| `subir-postgres.bat` | Só banco PostgreSQL |
| `subir-backend.bat` | Só API. `subir-backend.bat build` recompila o JAR |
| `subir-frontend.bat` | Só nginx/React. `subir-frontend.bat build` recompila o frontend |
| `subir-tunel.bat` | Só túnel Cloudflare (URL pública) |
| `subir-prod.bat` | Produção: `docker,prod`, sem seeds demo, sem túnel |
| `atualizar-url-publica.bat` | Regrava `URL_PUBLICA.txt` com a URL **mais recente** do túnel |
| `scripts/configurar-portas.py` | Portas livres + firewall Windows |
| `scripts/aguardar-url-publica.py` | Lê logs do túnel → `URL_PUBLICA.txt` |
| `scripts/subir-servico.ps1` | Lógica compartilhada dos `.bat` modulares |
| `scripts/subir-docker.ps1` | Mesmo fluxo em PowerShell |
| `backup-banco.bat` | Backup manual do PostgreSQL |
| `restaurar-banco.bat` | Restaura ultimo backup (ou arquivo informado) |
| `agendar-backup-semanal.bat` | Tarefa Windows: backup todo domingo 03:00 |
| [BACKUP_BANCO.md](./BACKUP_BANCO.md) | Backup automatico, restauracao e retencao |

### Restart parcial (mais rápido)

Após alterar só o backend:

```bat
subir-backend.bat build
```

Após alterar só o frontend:

```bat
subir-frontend.bat build
```

Reiniciar túnel sem rebuild da app:

```bat
subir-tunel.bat
```

Só o banco:

```bat
subir-postgres.bat
```

### Git Bash (MINGW64)

No Bash, use **`./`** antes do script (`.bat` não entra no PATH):

```bash
./subir-backend.sh build
./subir-frontend.sh build
./subir-tunel.sh
./subir-postgres.sh
./subir.sh              # stack completa
./subir.sh build        # stack + rebuild
```

Equivalente no CMD/PowerShell: `subir-backend.bat build` (sem `./`).

## URLs

| Acesso | URL |
|--------|-----|
| Sua máquina | http://localhost:5173 |
| Rede Wi‑Fi (opcional) | http://SEU_IP:5173 |
| **Internet (compartilhar)** | https://….trycloudflare.com (logs do `tunnel`) |
| Swagger (só local) | http://localhost:8000/srv-gerenciaracademia/swagger-ui.html |

## Variáveis `.env`

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `APP_PORT` | `5173` | Porta do app no host |
| `JWT_SECRET` | (exemplo) | Troque em ambiente público |
| `NGROK_AUTHTOKEN` | — | Opcional, URL estável |

## Desenvolvimento local (sem túnel)

```bash
docker compose up -d postgres backend frontend
```

Frontend dev: `frontend/.env.local` com `VITE_API_URL=http://localhost:8000/srv-gerenciaracademia`.

## Performance (CPU e memória)

O backend Spring Boot costumava consumir quase toda a RAM do host porque a JVM usava `MaxRAMPercentage` **sem limite no container**.

### Ajustes aplicados

| Área | O que mudou |
|------|-------------|
| **JVM** | Heap fixo (`-Xms256m -Xmx512m` dev, `-Xmx768m` prod) + G1GC |
| **Docker** | `mem_limit` / `cpus` no backend e postgres (`.env`: `BACKEND_MEM_LIMIT`, `BACKEND_CPUS`) |
| **Logging** | Docker/prod: só console, nível WARN/INFO (sem arquivo `logs/`) |
| **Startup** | `lazy-initialization` no profile docker; seeds demo desligáveis |
| **JPA/Tomcat** | Pool Hikari e threads Tomcat reduzidos em dev; `open-in-view=false` |
| **Produção** | Profile `prod`: `ddl-auto=validate`, sem Swagger, sem massa demo |

### Produção

**Windows:** execute **`subir-prod.bat`** na raiz do projeto.

**Linha de comando:**

```bash
docker compose -f docker-compose.yml -f infra/docker/docker-compose.prod.yml up -d --build
```

Variáveis úteis no `.env`:

| Variável | Padrão dev | Produção |
|----------|------------|----------|
| `BACKEND_MEM_LIMIT` | `768m` | `1024m` |
| `APP_SEED_DEMO` | `true` | `false` (via overlay prod) |
| `SPRING_PROFILES_ACTIVE` | `docker` | `docker,prod` |

### Monitorar uso

```bash
docker stats academia-backend academia-postgres
curl -s http://localhost:8000/srv-gerenciaracademia/actuator/health
```

Se a máquina for fraca (4 GB RAM), reduza no `.env`: `BACKEND_MEM_LIMIT=512m` e `JAVA_OPTS=-Xms128m -Xmx384m ...`.

## Parar

```bash
docker compose down
docker compose down -v   # apaga banco local
```
