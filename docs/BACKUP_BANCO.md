# Backup do banco PostgreSQL

Backups do banco `gerenciamento_academia` (container `academia-postgres`), comprimidos em **`.sql.gz`**, salvos em `backups/postgres/`.

## Quando o backup roda

| Gatilho | Quando |
|---------|--------|
| **Pre-restart** | Antes de `subir-postgres.bat` / `./subir-postgres.sh` (se o container ja existir) |
| **Semanal** | Ao subir qualquer servico (`subir.bat`, `subir-backend`, etc.) se o ultimo backup tiver **mais de 7 dias** e o Postgres estiver rodando |
| **Manual** | `backup-banco.bat` ou `./backup-banco.sh` |
| **Agendado (Windows)** | `agendar-backup-semanal.bat` — domingo 03:00 (Agendador de Tarefas) |

## Comandos

```bat
REM Backup manual
backup-banco.bat

REM Restaurar ultimo backup (pede confirmacao SIM)
restaurar-banco.bat

REM Restaurar arquivo especifico
restaurar-banco.bat -Arquivo backups\postgres\gerenciamento_academia_2026-06-17_12-00-00_manual.sql.gz

REM Agendar backup semanal no Windows
agendar-backup-semanal.bat
```

Git Bash:

```bash
./backup-banco.sh
./restaurar-banco.sh   # via PowerShell
```

## Configuracao (.env)

```env
# Quantos backups manter (mais antigos sao apagados)
BACKUP_RETENTION=14

# Dias sem backup antes do gatilho semanal automatico
BACKUP_WEEKLY_DAYS=7
```

## Restauracao

1. Pare o backend se possivel (`docker stop academia-backend`).
2. Execute `restaurar-banco.bat` e confirme digitando **SIM**.
3. Suba o backend: `subir-backend.bat`.

A restauracao **apaga e recria** o banco atual.

## Seguranca

- Backups ficam **apenas na maquina local** (`backups/postgres/`) — nao vao para o Git.
- Em producao, copie os `.sql.gz` para storage externo (nuvem, NAS, outro disco).
- Proteja o `.env` (contem `POSTGRES_PASSWORD`).

## Agendador Windows

`agendar-backup-semanal.bat` cria a tarefa `EduGestao-BackupPostgresSemanal`.

Requisito: **Docker Desktop em execucao** no horario do backup.

Remover:

```powershell
Unregister-ScheduledTask -TaskName 'EduGestao-BackupPostgresSemanal' -Confirm:$false
```
