@echo off
chcp 65001 >nul
cd /d "%~dp0"
title EduGestao - Atualizar URL publica

echo.
echo  Atualizar URL_PUBLICA.txt (tunel Cloudflare)
echo  ============================================
echo.

where docker >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker nao encontrado.
    pause
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker nao esta rodando.
    pause
    exit /b 1
)

where python >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Python nao encontrado. Instale Python 3 ou use:
    echo   docker compose logs tunnel --tail 30
    pause
    exit /b 1
)

python scripts\aguardar-url-publica.py
set RC=%ERRORLEVEL%

if %RC% neq 0 (
    echo.
    echo Nao foi possivel gravar a URL. Verifique se o tunel esta ativo:
    echo   docker compose up -d tunnel
    echo   docker compose logs tunnel --tail 30
    pause
    exit /b %RC%
)

if exist URL_PUBLICA.txt type URL_PUBLICA.txt
echo.
pause
