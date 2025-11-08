@echo off
REM Script para compilar el launcher universal en Windows

echo ═══════════════════════════════════════════════════════
echo   Compilando Launcher Universal
echo ═══════════════════════════════════════════════════════
echo.

REM Compilar el launcher
echo 1. Compilando Launcher.java...
if not exist target\launcher-classes mkdir target\launcher-classes
javac -d target\launcher-classes src\launcher\java\Launcher.java

if errorlevel 1 (
    echo ERROR: Fallo la compilacion del launcher
    exit /b 1
)

REM Crear el JAR del launcher
echo 2. Creando launcher.jar...
cd target\launcher-classes
jar cfe ..\launcher.jar Launcher *.class
cd ..\..

if errorlevel 1 (
    echo ERROR: Fallo la creacion del JAR
    exit /b 1
)

echo.
echo √ Launcher compilado exitosamente: target\launcher.jar
echo.
echo Para ejecutar la aplicacion, usa:
echo   java -jar target\launcher.jar
echo.
pause
