#!/usr/bin/env bash
# Backup PostgreSQL via docker exec (Git Bash / WSL).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

MOTIVO="${1:-manual}"
shift || true

PS_ARGS=(-Motivo "$MOTIVO")
while [[ $# -gt 0 ]]; do
  case "${1,,}" in
    force|forcar) PS_ARGS+=(-Forcar) ;;
    quiet) PS_ARGS+=(-Quiet) ;;
  esac
  shift
done

if command -v powershell.exe >/dev/null 2>&1; then
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$ROOT/scripts/backup-postgres.ps1" "${PS_ARGS[@]}"
  exit $?
fi
if command -v pwsh >/dev/null 2>&1; then
  pwsh -NoProfile -ExecutionPolicy Bypass -File "$ROOT/scripts/backup-postgres.ps1" "${PS_ARGS[@]}"
  exit $?
fi

echo "[ERRO] PowerShell necessario para backup neste ambiente." >&2
exit 1
