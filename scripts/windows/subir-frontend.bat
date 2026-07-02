@echo off
chcp 65001 >nul
cd /d "%~dp0\..\.."
title Turma360 - Subir Frontend

echo.
echo  Subir Frontend (nginx + React)
echo  ==============================
echo   Uso: subir-frontend.bat          ^(rapido, sem rebuild^)
echo        subir-frontend.bat build     ^(recompila npm^)
echo.

call scripts\_subir-wrapper.bat frontend %*
exit /b %ERRORLEVEL%
