# EduGestão Inteligente

Sistema SaaS para gestão de **instituições de ensino**: colaboradores, alunos, turmas, grade horária, financeiro, portal do aluno, permissões por perfil e auditoria.

## Estrutura do projeto

```
gerenciamentoDeAcademia/
├── backend/          # API Spring Boot (Java 17)
├── frontend/         # SPA React + TypeScript + Vite
├── docs/             # Roadmap, deploy, migrações, testes — índice em docs/README.md
├── infra/            # Docker/PostgreSQL init — ver infra/README.md
├── scripts/          # Portas, subida Docker, URL do túnel
├── docker-compose.yml
└── .env.example
```

Arquitetura detalhada: **[docs/ARQUITETURA.md](docs/ARQUITETURA.md)**.

## Stack

| Camada | Tecnologias |
|--------|-------------|
| Backend | Java 17, Spring Boot 2.7, JPA, JWT (Auth0), PostgreSQL / H2 |
| Frontend | React 18, TypeScript, Vite, React Router, styled-components |
| Infra | Docker Compose (PostgreSQL + API + frontend + túnel opcional) |

Pacotes: backend `com.lucastro-dev:gerenciamentoDeAcademia` · frontend `edugestao-inteligente-frontend` (`1.0.1-SNAPSHOT`).

## Execução rápida (Docker — teste na internet)

```bash
cp .env.example .env
docker compose up -d --build
```

**Windows:** duplo clique em **`subir.bat`** — sobe a stack e grava o link público em `URL_PUBLICA.txt`.

| Acesso | URL |
|--------|-----|
| Local | http://localhost:5173 |
| Externo (túnel) | `https://….trycloudflare.com` — ver `URL_PUBLICA.txt` ou `docker compose logs tunnel` |
| Swagger (local) | http://localhost:8000/srv-gerenciaracademia/swagger-ui.html |

- Passo a passo: **[PASSO_A_PASSO_DEPLOY.txt](PASSO_A_PASSO_DEPLOY.txt)**
- Deploy: **[docs/DEPLOY_DOCKER.md](docs/DEPLOY_DOCKER.md)**
- Usuários de teste: **[docs/USUARIOS_TESTE.md](docs/USUARIOS_TESTE.md)**

### Usuário master (profile `docker`)

| Campo | Valor padrão |
|-------|----------------|
| CPF (login) | `00000000191` |
| Senha | `Master@2024!` |
| Instituição | ID `1` (após seed) |

Altere em `.env`: `APP_MASTER_CPF`, `APP_MASTER_PASSWORD`.

## Desenvolvimento local

1. `cp .env.example .env`
2. Banco: `docker compose up postgres -d` (porta host **5435** — `POSTGRES_HOST_PORT`)
3. **Backend** (profile `local`, `ddl-auto=update`):

```bash
cd backend
mvn spring-boot:run
```

4. **Frontend**:

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

H2 em memória (testes rápidos): `SPRING_PROFILES_ACTIVE=h2 mvn spring-boot:run` (Windows: `set SPRING_PROFILES_ACTIVE=h2`).

### Erro `password authentication failed for user academia_app`

O PostgreSQL grava a senha na **primeira criação do volume**. Se a senha no `.env` mudou depois:

```bash
docker compose down -v
docker compose up postgres -d
```

Senha padrão: `academia_dev_secret` (usuário `academia_app`). Alinhe com `application-local.properties` se alterar.

## Perfis e segurança

Tipos de colaborador: `DIRETOR`, `TI`, `ADMINISTRADOR`, `FINANCEIRO`, `RH`, `RECEPCIONISTA`, `PROFESSOR`, `ESTAGIARIO`, `SERVICOS_GERAIS`, `TERCEIRIZADO`.

JWT + `@PreAuthorize`; ações críticas em `tb_auditoria`.

## Roadmap e fase atual

- Índice da documentação: **[docs/README.md](docs/README.md)**
- Planejamento completo: **[docs/ROADMAP.md](docs/ROADMAP.md)**  
- **Concluído:** operador master (jun/2026) e perfil **Professor** (jul/2026) — turmas, presença, certificados, consulta LGPD, programação.
- **Próximo:** perfis DIRETOR/ADMIN, RECEPÇÃO/RH, FINANCEIRO e LGPD — ver [docs/ROADMAP_PROXIMOS.md](docs/ROADMAP_PROXIMOS.md).

## Testes backend

```bash
cd backend
mvn test
```
