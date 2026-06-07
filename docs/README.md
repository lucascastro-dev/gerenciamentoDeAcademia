# Documentação — EduGestão Inteligente

Índice da pasta `docs/`. O [README principal](../README.md) cobre execução rápida; aqui estão roteiros, migrações e planejamento.

## Planejamento

| Documento | Conteúdo |
|-----------|----------|
| [ROADMAP.md](./ROADMAP.md) | Entregas concluídas (master + **professor**), próxima fase (perfis institucionais) e backlog |
| [ROADMAP_PROXIMOS.md](./ROADMAP_PROXIMOS.md) | Resumo do próximo foco (ago/2026) |
| [ARQUITETURA.md](./ARQUITETURA.md) | Estrutura do monorepo, camadas backend/frontend e convenções |

> `ROADMAP_PROXIMOS.md` foi unificado em `ROADMAP.md` (mantido só como redirecionamento).

## Deploy e ambiente

| Documento | Conteúdo |
|-----------|----------|
| [DEPLOY_DOCKER.md](./DEPLOY_DOCKER.md) | Docker, túnel Cloudflare, ngrok, variáveis `.env`, **limites de CPU/RAM** |
| [USUARIOS_TESTE.md](./USUARIOS_TESTE.md) | CPFs, senhas e perfis para teste |
| [CENARIOS_TESTE.md](./CENARIOS_TESTE.md) | Roteiro manual (grade, portal, cobrança, turmas) |

## Banco de dados (migrações manuais)

Execute na ordem se o PostgreSQL **já existia** antes da versão correspondente:

1. `MIGRACAO_ACADEMIA_INSTITUICAO.sql` — nomenclatura Academia → Instituição  
2. `MIGRACAO_PROGRAMACAO_SCHEMA.sql` — programação, salas, grade  
3. `MIGRACAO_DEDUP_ALUNO_CPF.sql` — CPF duplicado (legado)  
4. `SEED_CENARIOS_TESTE.sql` — opcional, cenários extras sem reiniciar o backend  

Banco novo via Docker: scripts em `infra/docker/postgres/init/` + seeds automáticos no startup (`local` / `docker`).
