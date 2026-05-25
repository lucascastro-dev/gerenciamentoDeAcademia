#!/usr/bin/env python3
"""Aguarda URL publica do tunel (cloudflared ou ngrok) e grava em URL_PUBLICA.txt."""
from __future__ import annotations

import re
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTAINER = "academia-tunnel"
SAIDA = ROOT / "URL_PUBLICA.txt"

PADROES = [
    re.compile(r"https://[a-z0-9-]+\.trycloudflare\.com", re.I),
    re.compile(r"https://[a-z0-9-]+\.ngrok-free\.app", re.I),
    re.compile(r"https://[a-z0-9-]+\.ngrok\.io", re.I),
]


def logs_tunnel() -> str:
    r = subprocess.run(
        ["docker", "logs", CONTAINER],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        cwd=ROOT,
    )
    return (r.stdout or "") + (r.stderr or "")


def extrair_url(texto: str) -> str | None:
    for padrao in PADROES:
        m = padrao.search(texto)
        if m:
            return m.group(0)
    return None


def main() -> int:
    print(f"Aguardando URL publica do container '{CONTAINER}' (ate 120s)...")
    url = None
    for _ in range(60):
        texto = logs_tunnel()
        url = extrair_url(texto)
        if url:
            break
        time.sleep(2)

    if not url:
        print("URL ainda nao apareceu. Tente: docker compose logs tunnel")
        return 1

    SAIDA.write_text(
        f"URL publica (compartilhe com testadores externos):\n{url}\n\n"
        f"Credenciais: docs/USUARIOS_TESTE.md\n",
        encoding="utf-8",
    )
    print("")
    print("=" * 60)
    print("  TESTE EXTERNO — copie e envie este link:")
    print(f"  {url}")
    print("=" * 60)
    print(f"  Salvo em: {SAIDA}")
    return 0


if __name__ == "__main__":
    import os

    os.chdir(ROOT)
    sys.exit(main())
