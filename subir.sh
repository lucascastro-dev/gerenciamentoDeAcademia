#!/usr/bin/env bash
# Sobe stack completa (postgres + backend + frontend + tunel).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
ARGS=(todos pull)
if [[ "${1:-}" == "build" ]] || [[ "${1:-}" == "--build" ]]; then
  ARGS+=(build)
fi
exec "$ROOT/scripts/subir-servico.sh" "${ARGS[@]}"
