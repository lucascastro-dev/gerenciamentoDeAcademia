@echo off
chcp 65001 >nul
cd /d "%~dp0\..\.."
title Turma360 - Subir Postgres

echo.
echo  Subir Postgres (banco de dados)
echo  ===============================
echo.

call scripts\_subir-wrapper.bat postgres %*
exit /b %ERRORLEVEL%
