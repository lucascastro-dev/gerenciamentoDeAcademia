@echo off
chcp 65001 >nul
cd /d "%~dp0\.."

where docker >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker nao encontrado. Instale o Docker Desktop.
    if not "%EDUGESTAO_QUIET%"=="1" pause
    exit /b 1
)

set "SERVICO=%~1"
set "EXTRA="
:parse
shift
if "%~1"=="" goto run
if /i "%~1"=="build" set "EXTRA=%EXTRA% -Build"
if /i "%~1"=="pull" set "EXTRA=%EXTRA% -Pull"
if /i "%~1"=="quiet" set "EXTRA=%EXTRA% -Quiet"
goto parse

:run
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0subir-servico.ps1" -Servico %SERVICO% %EXTRA%
exit /b %ERRORLEVEL%
