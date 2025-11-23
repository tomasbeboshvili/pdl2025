# Procesadores de Lenguajes – Analizador Léxico (Grupo 15)
- [Procesadores de Lenguajes – Analizador Léxico (Grupo 15)](#procesadores-de-lenguajes--analizador-léxico-grupo-15)
	- [Descripción General](#descripción-general)
	- [Grupo 15 – Opciones asignadas](#grupo-15--opciones-asignadas)
	- [Funcionalidad](#funcionalidad)
	- [Ficheros generados](#ficheros-generados)
	- [Estructura del proyecto](#estructura-del-proyecto)
	- [Compilación y ejecución](#compilación-y-ejecución)

## Descripción General
Este proyecto implementa el **Analizador Léxico y la Tabla de Símbolos** para el lenguaje **MyJS**, como parte de la asignatura **Procesadores de Lenguajes** (ETSII – UPM, curso 2025/2026).

El analizador identifica los **tokens** definidos para la versión del lenguaje asignada al **Grupo 15**, genera los ficheros de salida requeridos y reporta los errores léxicos detectados.

---

## Grupo 15 – Opciones asignadas

| Categoría | Opción asignada | Implementado |
|------------|------------------|---------------|
| **Sentencia repetitiva** | `for` | ✅ |
| **Técnica de análisis sintáctico** | Descendente con tablas | (fase 3) |
| **Operador especial** | Asignación con división `/=` | ✅ |
| **Comentarios** | Comentarios de línea `//` | ✅ |
| **Cadenas** | Comillas simples `' '` | ✅ |

---

## Funcionalidad

El **analizador léxico (`Lexer.java`)**:
- Lee un archivo fuente con extensión `.javascript` o `.txt`.
- Genera la **lista de tokens** correspondientes a las palabras clave, identificadores, constantes y símbolos del lenguaje.
- Detecta errores léxicos (símbolos no válidos, cadenas no cerradas, números fuera de rango…).
- Mantiene una **tabla de símbolos** con los identificadores encontrados.
- Omite los **comentarios de línea `//`**.
- Diferencia entre **enteros y reales** con validación de rangos:
  - Enteros: ≤ 32767  
  - Reales: ≤ 117549436.0

---

## Ficheros generados

Tras analizar un programa fuente, el procesador crea los siguientes ficheros dentro de `src/lexico/out`:

| Fichero | Descripción |
|----------|-------------|
| **tokens.txt** | Lista completa de tokens generados. |
| **tabla_simbolos.txt** | Volcado legible de la tabla de símbolos. |
| **errores.txt** | Listado de errores léxicos detectados (si los hay). |

---

## Estructura del proyecto

```
.
├── README.md
├── src/
│   ├── lexico/
│   │   ├── Lexer.java
│   │   ├── Main.java
│   │   ├── Token.java
│   │   ├── programa.javascript # Fuente a analizar
│   │   └── out/
│   │       ├── tokens.txt
│   │       ├── tabla_simbolos.txt
│   │       └── errores.txt
│   └── Sintactico/
│       ├── ASTNode.java
│       ├── Parser.java
│       ├── Main.java
│       └── out/
│           └── ast.dot
├── bin/
│   └── ...
└── analizador_paquete.zip
```

## Compilación y ejecución

Desde la raíz del proyecto:

```
# Compilar
javac -d bin src/lexico/*.java src/Sintactico/*.java

# Ejecutar
java -cp bin lexico.Main

# Ejecutar sintáctico (usa src/lexico/programa.javascript por defecto o pasa un archivo)
java -cp bin Sintactico.Main [ruta_al_fuente_opcional]
```
