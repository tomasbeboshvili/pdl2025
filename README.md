# 🧩 Procesadores de Lenguajes – Analizador Léxico (Grupo 15)

## 📘 Descripción General
Este proyecto implementa el **Analizador Léxico y la Tabla de Símbolos** para el lenguaje **MyJS**, como parte de la asignatura **Procesadores de Lenguajes** (ETSII – UPM, curso 2025/2026).

El analizador identifica los **tokens** definidos para la versión del lenguaje asignada al **Grupo 15**, genera los ficheros de salida requeridos y reporta los errores léxicos detectados.

---

## 🧑‍💻 Grupo 15 – Opciones asignadas

| Categoría | Opción asignada | Implementado |
|------------|------------------|---------------|
| **Sentencia repetitiva** | `for` | ✅ |
| **Técnica de análisis sintáctico** | Descendente con tablas | (fase 2) |
| **Operador especial** | Asignación con división `/=` | ✅ |
| **Comentarios** | Comentarios de línea `//` | ✅ |
| **Cadenas** | Comillas simples `' '` | ✅ |

---

## 🧠 Funcionalidad

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

## 🧾 Ficheros generados

Tras analizar un programa fuente, el procesador crea los siguientes ficheros dentro de `/out`:

| Fichero | Descripción |
|----------|-------------|
| **tokens.txt** | Lista completa de tokens generados. |
| **tabla_simbolos.txt** | Volcado legible de la tabla de símbolos. |
| **errores.txt** | Listado de errores léxicos detectados (si los hay). |

---

## 🗂️ Estructura del proyecto

```
.
├── README.md
├── programa.javascript # Fuente a analizar
├── src/
│ └── lexico/
│ ├── Lexer.java
│ ├── Main.java
│ ├── Symbol.java
│ ├── Token.java
│ └── TokenType.java
├── bin/
│ └── lexico/
│ ├── Lexer.class
│ ├── Main.class
│ ├── Symbol.class
│ ├── Token.class
│ └── TokenType.class
└── out/
├── tokens.txt
├── tabla_simbolos.txt
└── errores.txt
```

## ⚙️ Compilación y ejecución

Desde la raíz del proyecto:

```
# Compilar
javac -d bin src/lexico/*.java

# Ejecutar
java -cp bin lexico.Main
```