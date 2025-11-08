#!/bin/bash
# Script para compilar el launcher universal

echo "═══════════════════════════════════════════════════════"
echo "  Compilando Launcher Universal"
echo "═══════════════════════════════════════════════════════"
echo ""

# Compilar el launcher
echo "1. Compilando Launcher.java..."
javac -d target/launcher-classes src/launcher/java/Launcher.java

if [ $? -ne 0 ]; then
    echo "ERROR: Falló la compilación del launcher"
    exit 1
fi

# Crear el JAR del launcher
echo "2. Creando launcher.jar..."
cd target/launcher-classes
jar cfe ../launcher.jar Launcher *.class
cd ../..

if [ $? -ne 0 ]; then
    echo "ERROR: Falló la creación del JAR"
    exit 1
fi

echo ""
echo "✓ Launcher compilado exitosamente: target/launcher.jar"
echo ""
echo "Para ejecutar la aplicación, usa:"
echo "  java -jar target/launcher.jar"
echo ""
