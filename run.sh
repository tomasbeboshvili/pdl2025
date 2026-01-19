#!/bin/bash

# Crear directorio bin si no existe
mkdir -p bin

# Compilar todo el proyecto
echo "Compilando..."
javac -encoding UTF-8 -d bin -cp src src/analizador/*.java

if [ $? -eq 0 ]; then
    echo "Compilación exitosa."
else
    echo "Error de compilación."
    exit 1
fi

# Ejecutar
if [ -z "$1" ]; then
    # Si no se pasa argumento, usar el fichero de prueba por defecto
    ARCHIVO="src/analizador/programa.javascript"
else
    ARCHIVO="$1"
fi

echo "Ejecutando Procesador con: $ARCHIVO"
java -cp bin analizador.Main "$ARCHIVO"
