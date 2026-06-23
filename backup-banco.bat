@echo off
chcp 65001 >nul
cd /d "%~dp0"
title EduGestao - Backup do banco

echo.
echo  Backup PostgreSQL (gerenciamento_academia)
echo  ==========================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\backup-postgres.ps1" -Motivo manual
set RC=%ERRORLEVEL%
if %RC% neq 0 pause
exit /b %RC%
