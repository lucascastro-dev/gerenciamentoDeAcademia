# Backups PostgreSQL

Os arquivos `.sql.gz` gerados ficam nesta pasta (ignorados pelo Git).

Gerados automaticamente por:

- `subir.bat` / `./subir.sh` (backup semanal na subida completa e antes de reiniciar o Postgres)
- `backup-banco.bat` / `./backup-banco.sh` (manual)
- Tarefa agendada via `agendar-backup-semanal.bat`

Documentacao: [docs/BACKUP_BANCO.md](../docs/BACKUP_BANCO.md)
