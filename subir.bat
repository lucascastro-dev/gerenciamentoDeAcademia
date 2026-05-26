@echo off
chcp 65001 >nul
cd /d "%~dp0"
title EduGestao - Subir aplicacao

echo.
echo  EduGestao - Deploy Docker + tunel publico
echo  ==========================================
echo.

where docker >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker nao encontrado. Instale o Docker Desktop e reinicie.
    pause
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker nao esta rodando. Abra o Docker Desktop e aguarde ficar pronto.
    pause
    exit /b 1
)

if not exist ".env" (
    copy /Y ".env.example" ".env" >nul
    echo [OK] Arquivo .env criado. Edite senhas se for ambiente publico.
    echo.
)

where python >nul 2>&1
if errorlevel 1 (
    echo [AVISO] Python nao encontrado; pulando verificacao de portas.
) else (
    python scripts\configurar-portas.py
    if errorlevel 1 (
        echo.
        echo Corrija as portas e execute subir.bat novamente.
        pause
        exit /b 1
    )
)

echo.
echo Subindo containers (build na primeira vez pode demorar)...
echo.

docker compose up -d --build

if errorlevel 1 (
    echo.
    echo ================= ERRO DETALHADO =================
    docker compose logs backend
    echo =================================================
    echo.
    pause
    exit /b 1
)

echo.
echo Aguardando backend e tunel...
timeout /t 15 /nobreak >nul

where python >nul 2>&1
if not errorlevel 1 (
    python scripts\aguardar-url-publica.py
) else (
    echo.
    echo Obtenha a URL publica com:
    echo   docker compose logs tunnel
    echo Procure um link https://....trycloudflare.com
)

for /f "tokens=2 delims==" %%a in ('findstr /B "APP_PORT=" .env 2^>nul') do set APP_PORT=%%a
if not defined APP_PORT set APP_PORT=5173

echo.
echo  Local:  http://localhost:%APP_PORT%
echo  Logs:   docker compose logs -f backend
echo  Parar:  docker compose down
echo.
pause
