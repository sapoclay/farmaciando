#!/bin/bash
# Launcher universal para Linux/Mac
# Este script compila (si es necesario) y ejecuta la aplicación

echo "═══════════════════════════════════════════════════════"
echo "  FarmaCiando - Sistema de Gestión de Farmacia"
echo "═══════════════════════════════════════════════════════"
echo ""

# Verificar si existe el JAR principal
if [ ! -f "target/gestion-farmacia-1.0.0.jar" ]; then
    echo "El archivo JAR principal no existe."
    echo "Compilando el proyecto..."
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "ERROR: Falló la compilación del proyecto"
        exit 1
    fi
fi

# Verificar si existe el launcher
if [ ! -f "target/launcher.jar" ]; then
    echo "Compilando launcher universal..."
    bash compilar-launcher.sh
    
    if [ $? -ne 0 ]; then
        echo "ERROR: Falló la compilación del launcher"
        exit 1
    fi
fi

# Ejecutar la aplicación con el launcher
echo "Iniciando aplicación..."
echo ""
java -jar target/launcher.jar
