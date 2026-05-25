# Deploy com Docker (rede local e internet)

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

API e frontend usam o **mesmo host** (nginx faz proxy) — funciona para quem acessa de qualquer lugar.

### URL fixa (ngrok)

1. Conta em https://ngrok.com → copie o authtoken.
2. No `.env`: `NGROK_AUTHTOKEN=...`
3. Suba com:

```bash
docker compose -f docker-compose.yml -f docker-compose.ngrok.yml up -d --build
```

## Scripts

| Arquivo | Função |
|---------|--------|
| `subir.bat` | Verifica Docker, portas, sobe compose, mostra URL pública |
| `scripts/configurar-portas.py` | Portas livres + firewall Windows |
| `scripts/aguardar-url-publica.py` | Lê logs do túnel → `URL_PUBLICA.txt` |
| `scripts/subir-docker.ps1` | Mesmo fluxo em PowerShell |

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

## Parar

```bash
docker compose down
docker compose down -v   # apaga banco local
```
