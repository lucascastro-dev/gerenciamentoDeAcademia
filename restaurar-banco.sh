#!/usr/bin/env bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
if command -v powershell.exe >/dev/null 2>&1; then
  exec powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$ROOT/scripts/restaurar-backup-postgres.ps1" "$@"
fi
echo "[ERRO] PowerShell necessario." >&2
exit 1
