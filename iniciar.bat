@echo off
REM Launcher universal para Windows
REM Este script compila (si es necesario) y ejecuta la aplicacion

echo ═══════════════════════════════════════════════════════
echo   FarmaCiando - Sistema de Gestion de Farmacia
echo ═══════════════════════════════════════════════════════
echo.

REM Verificar si existe el JAR principal
if not exist target\gestion-farmacia-1.0.0.jar (
    echo El archivo JAR principal no existe.
    echo Compilando el proyecto...
    call mvn clean package -DskipTests
    
    if errorlevel 1 (
        echo ERROR: Fallo la compilacion del proyecto
        pause
        exit /b 1
    )
)

REM Verificar si existe el launcher
if not exist target\launcher.jar (
    echo Compilando launcher universal...
    call compilar-launcher.bat
    
    if errorlevel 1 (
        echo ERROR: Fallo la compilacion del launcher
        pause
        exit /b 1
    )
)

REM Ejecutar la aplicacion con el launcher
echo Iniciando aplicacion...
echo.
java -jar target\launcher.jar

pause
