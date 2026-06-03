# Infraestrutura

Arquivos de deploy e banco para ambientes **local**, **Docker** e túnel público (testes externos).

## Estrutura

```
infra/
└── docker/
    └── postgres/
        └── init/          # scripts na primeira subida do volume PostgreSQL
```

Os manifests Compose ficam na **raiz** do repositório (`docker-compose.yml`, variantes `demo` e `ngrok`).

## Comandos usuais

| Objetivo | Comando |
|----------|---------|
| Stack completa + túnel | `docker compose up -d --build` ou `subir.bat` |
| Só banco (dev local) | `docker compose up postgres -d` |
| API + front sem túnel | `docker compose up -d postgres backend frontend` |
| Parar e apagar volume | `docker compose down -v` |

Documentação detalhada: [docs/DEPLOY_DOCKER.md](../docs/DEPLOY_DOCKER.md).

## Variáveis

Copie `.env.example` para `.env` na raiz. Principais chaves: `POSTGRES_HOST_PORT`, `POSTGRES_PASSWORD`, `JWT_SECRET`, `APP_MASTER_*`, `APP_PORT`.

## Scripts auxiliares

Pasta `scripts/`: portas no Windows, URL pública do túnel (`aguardar-url-publica.py`), `subir-docker.ps1` / `.sh`.
