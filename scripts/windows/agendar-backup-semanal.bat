@echo off
chcp 65001 >nul
cd /d "%~dp0\..\.."
title Turma360 - Agendar backup semanal

echo.
echo  Agendar backup semanal (Agendador de Tarefas Windows)
echo  =====================================================
echo   Padrao: domingo as 03:00
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\agendar-backup-semanal.ps1" %*
pause
