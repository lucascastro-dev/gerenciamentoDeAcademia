#!/usr/bin/env bash
# Sobe a aplicacao com Docker Compose e exibe URLs para testadores na rede.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

DEMO=false
if [[ "${1:-}" == "--demo" ]]; then
  DEMO=true
fi

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Arquivo .env criado. Revise senhas antes de expor na rede."
fi

APP_PORT=5173
if [[ -f .env ]] && grep -qE '^APP_PORT=' .env; then
  APP_PORT="$(grep -E '^APP_PORT=' .env | head -1 | cut -d= -f2 | tr -d ' \r')"
fi

if $DEMO; then
  echo "Modo demo: apenas porta ${APP_PORT} (API via proxy nginx)."
  docker compose -f docker-compose.yml -f docker-compose.demo.yml up -d --build
else
  docker compose up -d --build
fi

sleep 5
docker compose ps

IP="$(hostname -I 2>/dev/null | awk '{print $1}' || true)"
if [[ -z "$IP" ]]; then
  IP="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{print $7; exit}' || echo "")"
fi

echo ""
echo "=== URLs ==="
echo "  Nesta máquina:  http://localhost:${APP_PORT}"
if [[ -n "$IP" ]]; then
  echo "  Rede local:     http://${IP}:${APP_PORT}  (compartilhe com testadores)"
fi
echo "  Credenciais:    docs/USUARIOS_TESTE.md"
echo ""
echo "Logs: docker compose logs -f backend"
