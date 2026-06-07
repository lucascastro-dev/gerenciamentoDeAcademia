#!/usr/bin/env python3
"""Aguarda URL publica do tunel (cloudflared ou ngrok) e grava em URL_PUBLICA.txt."""
from __future__ import annotations

import re
import subprocess
import sys
import time
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTAINER = "academia-tunnel"
SAIDA = ROOT / "URL_PUBLICA.txt"

PADROES = [
    re.compile(r"https://[a-z0-9-]+\.trycloudflare\.com", re.I),
    re.compile(r"https://[a-z0-9-]+\.ngrok-free\.app", re.I),
    re.compile(r"https://[a-z0-9-]+\.ngrok\.io", re.I),
]


def container_existe() -> bool:
    r = subprocess.run(
        ["docker", "inspect", "-f", "{{.State.Running}}", CONTAINER],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        cwd=ROOT,
    )
    return r.returncode == 0 and (r.stdout or "").strip().lower() == "true"


def logs_tunnel(*, since: str | None = None, tail: int | None = 800) -> str:
    cmd = ["docker", "logs"]
    if since:
        cmd.extend(["--since", since])
    elif tail:
        cmd.extend(["--tail", str(tail)])
    cmd.append(CONTAINER)

    r = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        cwd=ROOT,
    )
    return (r.stdout or "") + (r.stderr or "")


def extrair_urls(texto: str) -> list[str]:
    """Todas as URLs encontradas, na ordem em que aparecem nos logs."""
    encontradas: list[str] = []
    for padrao in PADROES:
        encontradas.extend(padrao.findall(texto))
    return encontradas


def extrair_url_mais_recente(texto: str) -> str | None:
    """Ultima URL nos logs = tunel ativo apos restart (nao a primeira da historia)."""
    urls = extrair_urls(texto)
    return urls[-1] if urls else None


def gravar_url(url: str) -> None:
    agora = datetime.now(timezone.utc).astimezone().strftime("%d/%m/%Y %H:%M:%S")
    SAIDA.write_text(
        f"URL publica (compartilhe com testadores externos):\n{url}\n\n"
        f"Atualizado em: {agora}\n"
        f"Credenciais: docs/USUARIOS_TESTE.md\n",
        encoding="utf-8",
    )


def main() -> int:
    if not container_existe():
        print(f"[ERRO] Container '{CONTAINER}' nao esta em execucao.")
        print("       Suba o tunel: docker compose up -d tunnel")
        return 1

    print(f"Aguardando URL publica do container '{CONTAINER}' (ate 120s)...")
    url: str | None = None

    for tentativa in range(60):
        # Janela recente primeiro; depois cauda dos logs para pegar a URL mais nova
        texto = logs_tunnel(since="10m") if tentativa < 20 else logs_tunnel(tail=800)
        url = extrair_url_mais_recente(texto)
        if url:
            break
        time.sleep(2)

    if not url:
        print("URL ainda nao apareceu. Tente: docker compose logs tunnel --tail 50")
        return 1

    gravar_url(url)
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
