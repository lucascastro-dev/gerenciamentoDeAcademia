#!/usr/bin/env bash
# Wrapper Bash (Git Bash / WSL) — delega para scripts/subir-servico.ps1 no Windows.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

SERVICO="${1:?Uso: subir-servico.sh <postgres|backend|frontend|tunnel|todos> [build] [pull] [quiet]}"
shift

PS_ARGS=(-Servico "$SERVICO")
while [[ $# -gt 0 ]]; do
  case "${1,,}" in
    build) PS_ARGS+=(-Build) ;;
    pull)  PS_ARGS+=(-Pull) ;;
    quiet) PS_ARGS+=(-Quiet) ;;
    *)
      echo "[ERRO] Argumento desconhecido: $1 (use: build, pull, quiet)" >&2
      exit 1
      ;;
  esac
  shift
done

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERRO] Docker nao encontrado. Instale o Docker Desktop." >&2
  exit 1
fi

run_powershell() {
  if command -v powershell.exe >/dev/null 2>&1; then
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$ROOT/scripts/subir-servico.ps1" "${PS_ARGS[@]}"
    return
  fi
  if command -v pwsh >/dev/null 2>&1; then
    pwsh -NoProfile -ExecutionPolicy Bypass -File "$ROOT/scripts/subir-servico.ps1" "${PS_ARGS[@]}"
    return
  fi
  echo "[ERRO] PowerShell nao encontrado (powershell.exe / pwsh)." >&2
  echo "       No Git Bash use: ./subir-backend.sh build" >&2
  echo "       Ou no CMD:       subir-backend.bat build" >&2
  exit 1
}

run_powershell
