@echo off
chcp 65001 >nul
cd /d "%~dp0"
title EduGestao - Restaurar backup

echo.
echo  Restaurar backup PostgreSQL
echo  =============================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\restaurar-backup-postgres.ps1" %*
pause
