# Projeto Gerenciamento de Academia / Instituição de Ensino

Sistema para gestão de academias e escolas: alunos, funcionários, turmas, certificados, permissões por perfil e auditoria.

## Stack

| Camada | Tecnologias |
|--------|-------------|
| Backend | Java 17, Spring Boot 2.7, JPA, JWT, PostgreSQL / H2 |
| Frontend | React 18, TypeScript, Vite, React Router |
| Infra | Docker Compose (PostgreSQL + API + frontend) |

## Execução rápida (Docker — PostgreSQL)

```bash
cp .env.example .env
docker compose up -d --build
```

- API: http://localhost:8000/srv-gerenciaracademia  
- Frontend: http://localhost:5173  
- Swagger: http://localhost:8000/srv-gerenciaracademia/swagger-ui.html  

### Usuário master (criado automaticamente no profile `docker`)

| Campo | Valor padrão |
|-------|----------------|
| CPF (login) | `00000000191` |
| Senha | `Master@2024!` |
| Código academia | ID `1` (após seed) |

Altere credenciais em `.env` (`APP_MASTER_CPF`, `APP_MASTER_PASSWORD`).

## Desenvolvimento local (PostgreSQL persistente)

1. Crie o arquivo de ambiente (senha alinhada com o backend):

```bash
cp .env.example .env
```

2. Suba o banco:

```bash
docker compose up postgres -d
```

**Backend** (profile `local` é o padrão — `ddl-auto=update`)

```bash
cd backend
mvn spring-boot:run
```

### Erro `password authentication failed for user academia_app`

O PostgreSQL grava a senha **na primeira criação do volume**. Se você mudou a senha depois, o container sobe mas a senha interna continua a antiga.

**Solução (apaga dados locais do banco):**

```bash
docker compose down -v
docker compose up postgres -d
```

Depois rode o backend de novo. Senha padrão: `academia_dev_secret` (usuário `academia_app`).

### Porta do PostgreSQL

O projeto usa a porta **5435** no host (veja `POSTGRES_HOST_PORT` no `.env`), para não conflitar com outros Postgres no Docker (5432, 5433, 5434). Se precisar mudar, altere `.env` e `application-local.properties` juntos.

Para H2 em memória (testes rápidos): `set SPRING_PROFILES_ACTIVE=h2` (Windows) ou `SPRING_PROFILES_ACTIVE=h2 mvn spring-boot:run`

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

## Perfis de funcionário e segurança

Tipos: `DIRETOR`, `TI`, `ADMINISTRADOR` (master — acesso total), `FINANCEIRO`, `RH`, `RECEPCIONISTA`, `PROFESSOR`, `ESTAGIARIO`, `SERVICOS_GERAIS`, `TERCEIRIZADO`.

Permissões granulares via JWT + `@PreAuthorize`. Ações críticas registradas em `tb_auditoria`.

## Roadmap

Ver [docs/ROADMAP.md](docs/ROADMAP.md) para funcionalidades planejadas (financeiro completo, portal do aluno, multi-tenant, etc.).

## Testes backend

```bash
cd backend
mvn test
```
