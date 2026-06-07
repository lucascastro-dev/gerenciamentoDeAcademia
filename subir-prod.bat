@echo off
chcp 65001 >nul
cd /d "%~dp0"
title EduGestao - Subir aplicacao (PRODUCAO)

set COMPOSE_CMD=docker compose -f docker-compose.yml -f docker-compose.prod.yml

echo.
echo  EduGestao - Deploy Docker PRODUCAO
echo  ==================================
echo  Profile: docker,prod  ^|  Sem seeds demo  ^|  Sem tunel publico
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
    echo [OK] Arquivo .env criado a partir do exemplo.
    echo [AVISO] Edite JWT_SECRET, POSTGRES_PASSWORD e APP_MASTER_PASSWORD antes de expor na internet.
    echo.
)

findstr /C:"change-me-in-production" .env >nul 2>&1
if not errorlevel 1 (
    echo [AVISO] JWT_SECRET ainda parece ser o valor de exemplo no .env.
    echo         Troque antes de usar em producao real.
    echo.
)

where python >nul 2>&1
if errorlevel 1 (
    echo [AVISO] Python nao encontrado; pulando verificacao de portas.
) else (
    python scripts\configurar-portas.py
    if errorlevel 1 (
        echo.
        echo Corrija as portas e execute subir-prod.bat novamente.
        pause
        exit /b 1
    )
)

echo.
echo Baixando imagens base...
echo.

where powershell >nul 2>&1
if errorlevel 1 (
    docker pull public.ecr.aws/docker/library/node:20-alpine
    docker pull public.ecr.aws/docker/library/nginx:alpine
    docker pull public.ecr.aws/docker/library/postgres:16-alpine
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\baixar-imagens-docker.ps1"
    if errorlevel 1 (
        echo.
        echo Teste: docker pull public.ecr.aws/docker/library/node:20-alpine
        echo Depois execute subir-prod.bat novamente.
        pause
        exit /b 1
    )
)

echo.
echo Subindo postgres + backend + frontend (producao)...
echo Build na primeira vez pode demorar alguns minutos.
echo.

%COMPOSE_CMD% up -d --build

if errorlevel 1 (
    echo.
    echo ================= ERRO DETALHADO =================
    %COMPOSE_CMD% logs frontend
    %COMPOSE_CMD% logs backend
    echo =================================================
    echo.
    pause
    exit /b 1
)

echo.
echo Aguardando backend ficar pronto...
timeout /t 20 /nobreak >nul

for /f "tokens=2 delims==" %%a in ('findstr /B "APP_PORT=" .env 2^>nul') do set APP_PORT=%%a
if not defined APP_PORT set APP_PORT=5173

echo.
echo  App:     http://localhost:%APP_PORT%
echo  Health:  http://localhost:8000/srv-gerenciaracademia/actuator/health
echo  Logs:    %COMPOSE_CMD% logs -f backend
echo  Stats:   docker stats academia-backend academia-postgres
echo  Parar:   %COMPOSE_CMD% down
echo.
echo  Tunel publico (opcional, nao recomendado em prod):
echo    %COMPOSE_CMD% --profile tunnel up -d tunnel
echo.
pause
