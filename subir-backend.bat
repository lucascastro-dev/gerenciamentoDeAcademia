@echo off
chcp 65001 >nul
cd /d "%~dp0"
title EduGestao - Subir Backend

echo.
echo  Subir Backend (API Spring Boot)
echo  ===============================
echo   Uso: subir-backend.bat          ^(rapido, sem rebuild^)
echo        subir-backend.bat build     ^(recompila JAR^)
echo        subir-backend.bat pull build
echo.

call scripts\_subir-wrapper.bat backend %*
exit /b %ERRORLEVEL%
