package lexico;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Programa principal del Procesador de Lenguajes - Analizador Léxico.
 * 
 * Lee un fichero fuente, ejecuta el analizador léxico y genera:
 *  - tokens.txt: lista de tokens reconocidos
 *  - tabla_simbolos.txt: volcado de la tabla de símbolos
 *  - errores.txt: listado de errores léxicos detectados
 *
 * @author Grupo 15
 * @version Curso 2025/2026
 */
public class Main {

    public static void main(String[] args) {
        // Base del paquete "lexico" dentro de src para fuentes y salidas
        Path baseDir = Paths.get("src", "lexico");
        Path inputFile = baseDir.resolve("programa.javascript");
        Path outputDir = baseDir.resolve("out");

        // Crear carpeta de salida si no existe
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            System.err.println("Error creando carpeta de salida: " + e.getMessage());
            return;
        }

        // Leer el archivo fuente
        String source;
        try {
            source = Files.readString(inputFile);
        } catch (IOException e) {
            System.err.println("No se pudo leer el archivo fuente: " + inputFile);
            return;
        }

        // Ejecutar el analizador léxico
        Lexer lexer = new Lexer(source);

        // Guardar errores en un buffer en lugar de imprimirlos directamente
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorStream));

        List<Token> tokens = lexer.tokenize();

        // Restaurar la salida de error original
        System.setErr(originalErr);

        // Guardar tokens en archivo
        try (BufferedWriter writer = Files.newBufferedWriter(outputDir.resolve("tokens.txt"))) {
            for (Token token : tokens) {
                writer.write(token.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo tokens.txt: " + e.getMessage());
        }

        // Guardar tabla de símbolos
        try (BufferedWriter writer = Files.newBufferedWriter(outputDir.resolve("tabla_simbolos.txt"))) {
            writer.write(lexer.printSymbolTable());
        } catch (IOException e) {
            System.err.println("Error escribiendo tabla_simbolos.txt: " + e.getMessage());
        }

        // Guardar errores (si los hay)
        String errores = errorStream.toString().trim();
        try (BufferedWriter writer = Files.newBufferedWriter(outputDir.resolve("errores.txt"))) {
            if (errores.isEmpty()) {
                writer.write("Sin errores léxicos detectados.\n");
            } else {
                writer.write(errores);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo errores.txt: " + e.getMessage());
        }

        System.out.println("✅ Análisis léxico completado.\nResultados generados en la carpeta: " + outputDir);
    }
}
