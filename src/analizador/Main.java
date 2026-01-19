package analizador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Punto de entrada para probar el analizador sintáctico.
 * Lee el mismo programa de ejemplo que el léxico y genera
 * un fichero DOT del AST y un log de errores (si los hay).
 */
@SuppressWarnings("unused")
public class Main {

	public static void main(String[] args) {
		Path outDir = Paths.get("out");
		Path fuente = args.length > 0
				? Paths.get(args[0])
				: Paths.get("programa.javascript");

		try {
			Files.createDirectories(outDir);
		} catch (IOException e) {
			System.err.println("No se pudo crear el directorio de salida: " + e.getMessage());
			return;
		}

		String source;
		try {
			source = Files.readString(fuente);
		} catch (IOException e) {
			System.err.println("No se pudo leer el archivo fuente: " + fuente.toAbsolutePath());
			return;
		}

		// Gestor de errores compartido
		ErrorManager errorManager = new ErrorManager();

		// Analizador léxico
		Lexer lexer = new Lexer(source, errorManager);
		List<Token> tokens = lexer.tokenize();

		// Analizador sintáctico
		Parser parser = new Parser(tokens, errorManager);
		ASTNode ast = parser.parseAST();

		// --- GENERACIÓN DE FICHEROS ENTREGABLES ---

		// 1. Fichero de tokens
		Path tokensPath = outDir.resolve("tokens.txt");
		try (java.io.BufferedWriter writer = Files.newBufferedWriter(tokensPath)) {
			for (Token token : tokens) {
				writer.write(token.toString());
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("No se pudo escribir tokens.txt: " + e.getMessage());
		}

		// 2. Fichero de Tabla de Símbolos
		Path tablaPath = outDir.resolve("tabla_simbolos.txt");
		try {
			Files.writeString(tablaPath, parser.getTS().toString());
		} catch (IOException e) {
			System.err.println("No se pudo escribir tabla_simbolos.txt: " + e.getMessage());
		}

		// 3. Fichero del parse (Reglas para VAST)
		// Nota: parse_reglas.txt es el que pide la práctica como 'parse'
		Path reglasPath = outDir.resolve("parse.txt");
		try {
			Files.writeString(reglasPath, parser.getReglasAplicadasLinea());
		} catch (IOException e) {
			System.err.println("No se pudo escribir el parse de reglas: " + e.getMessage());
		}

		// 4. Listado de errores
		Path errPath = outDir.resolve("errores.txt");
		try {
			if (errorManager.hayErrores()) {
				Files.writeString(errPath, errorManager.getErroresString());
				System.out.println("Se han detectado errores, para verlos vaya a " + errPath);
			} else {
				Files.writeString(errPath, "Sin errores detectados.\n");
				System.out.println("Análisis completado sin errores.");
			}
		} catch (IOException e) {
			System.err.println("No se pudo escribir el archivo de errores: " + e.getMessage());
		}

		System.out.println("\nFicheros generados en: " + outDir.toAbsolutePath());
		System.out.println(" - tokens.txt");
		System.out.println(" - tabla_simbolos.txt");
		System.out.println(" - parse.txt (Entrada para VASt)");
	}
}
