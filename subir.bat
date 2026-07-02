@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Turma360 - Subir aplicacao

echo.
echo  Turma360 - Deploy Docker + tunel publico
echo  ==========================================
echo.
echo  Servicos separados (restart parcial) — pasta scripts\windows\:
echo    scripts\windows\subir-postgres.bat
echo    scripts\windows\subir-backend.bat   [build]
echo    scripts\windows\subir-frontend.bat  [build]
echo    scripts\windows\subir-tunel.bat
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

set "BUILD_FLAG="
if /i "%~1"=="build" set "BUILD_FLAG=-Build"

echo Subindo stack completa (sem rebuild por padrao; use: subir.bat build)
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\subir-servico.ps1" -Servico todos -Pull %BUILD_FLAG%
set RC=%ERRORLEVEL%

if %RC% neq 0 (
    echo.
    echo Falha ao subir. Veja logs: docker compose logs --tail 50
    pause
    exit /b %RC%
)

for /f "tokens=2 delims==" %%a in ('findstr /B "APP_PORT=" .env 2^>nul') do set APP_PORT=%%a
if not defined APP_PORT set APP_PORT=5173

echo.
echo  Local:  http://localhost:%APP_PORT%
echo  Parar:  docker compose down
echo.
pause
