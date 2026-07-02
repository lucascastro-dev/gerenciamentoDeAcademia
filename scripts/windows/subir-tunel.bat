@echo off
chcp 65001 >nul
cd /d "%~dp0\..\.."
title Turma360 - Subir Tunel

echo.
echo  Subir Tunel (Cloudflare - URL publica)
echo  ======================================
echo   Requer frontend rodando. Apos reiniciar o tunel, a URL muda.
echo   Use atualizar-url-publica.bat se a URL nao aparecer aqui.
echo.

call scripts\_subir-wrapper.bat tunnel %*
exit /b %ERRORLEVEL%
