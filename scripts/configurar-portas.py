#!/usr/bin/env python3
"""Verifica portas do .env e configura firewall no Windows (se administrador)."""
from __future__ import annotations

import os
import re
import socket
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def ler_env() -> dict[str, str]:
    env_path = ROOT / ".env"
    valores: dict[str, str] = {}
    if not env_path.exists():
        return valores
    for linha in env_path.read_text(encoding="utf-8").splitlines():
        linha = linha.strip()
        if not linha or linha.startswith("#") or "=" not in linha:
            continue
        chave, valor = linha.split("=", 1)
        valores[chave.strip()] = valor.strip().strip('"').strip("'")
    return valores


def porta_livre(porta: int, host: str = "0.0.0.0") -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            s.bind((host, porta))
            return True
        except OSError:
            return False


def firewall_windows(portas: list[int]) -> None:
    if sys.platform != "win32":
        print("Firewall: configure manualmente as portas", portas, "(Linux: ufw allow).")
        return
    import ctypes

    if not ctypes.windll.shell32.IsUserAnAdmin():
        print("Firewall: execute como Administrador para liberar portas automaticamente.")
        print("         Ou libere manualmente no Painel do Windows:", portas)
        return
    for porta in portas:
        nome = f"EduGestao-TCP-{porta}"
        subprocess.run(
            [
                "netsh",
                "advfirewall",
                "firewall",
                "add",
                "rule",
                f"name={nome}",
                "dir=in",
                "action=allow",
                "protocol=TCP",
                f"localport={porta}",
            ],
            check=False,
            capture_output=True,
        )
        print(f"Firewall: regra '{nome}' (porta {porta}).")


def main() -> int:
    os.chdir(ROOT)
    env = ler_env()
    app_port = int(env.get("APP_PORT", "5173"))
    pg_port = int(env.get("POSTGRES_HOST_PORT", "5435"))
    api_port = 8000

    print("=== Configuracao de portas ===")
    print(f"APP_PORT={app_port}  POSTGRES_HOST_PORT={pg_port}  API(local)={api_port}")

    ocupadas = []
    for nome, porta in (("App", app_port), ("PostgreSQL", pg_port), ("API local", api_port)):
        if porta_livre(porta):
            print(f"  OK  {nome} :{porta}")
        else:
            print(f"  EM USO  {nome} :{porta}  <- pare o processo ou altere APP_PORT no .env")
            ocupadas.append(porta)

    firewall_windows([app_port])

    if ocupadas:
        print("\nCorrija portas em uso antes de subir o Docker.")
        return 1
    print("\nPortas prontas para docker compose up.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
