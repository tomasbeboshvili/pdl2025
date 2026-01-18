package Sintactico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lexico.Lexer;
import lexico.Token;

/**
 * Punto de entrada para probar el analizador sintáctico.
 * Lee el mismo programa de ejemplo que el léxico y genera
 * un fichero DOT del AST y un log de errores (si los hay).
 */
public class Main {

	public static void main(String[] args) {
		Path outDir = Paths.get("src", "Sintactico", "out");
		Path fuente = args.length > 0
				? Paths.get(args[0])
				: Paths.get("src", "lexico", "programa.javascript");

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

		// Analizador léxico
		Lexer lexer = new Lexer(source);
		List<Token> tokens = lexer.tokenize();

		// Analizador sintáctico
		Parser parser = new Parser(tokens);
		ASTNode ast = parser.parseAST();

		// Guardar AST en DOT
		Path dotPath = outDir.resolve("ast.dot");
		try {
			Files.writeString(dotPath, ast.toDotFile());
		} catch (IOException e) {
			System.err.println("No se pudo escribir el AST: " + e.getMessage());
		}

		// Guardar parse en texto indentado
		Path parsePath = outDir.resolve("parse.txt");
		try {
			Files.writeString(parsePath, ast.toIndentedString());
		} catch (IOException e) {
			System.err.println("No se pudo escribir el parse: " + e.getMessage());
		}

		// Guardar secuencia de reglas aplicadas (para VAST)
		Path reglasPath = outDir.resolve("parse_reglas.txt");
		try {
			Files.writeString(reglasPath, parser.getReglasAplicadasLinea());
		} catch (IOException e) {
			System.err.println("No se pudo escribir el parse de reglas: " + e.getMessage());
		}

		// Guardar errores (si los hay)
		Path errPath = outDir.resolve("errores.txt");
		String errores = parser.getErrores();
		try {
			if (parser.hasErrores()) {
				Files.writeString(errPath, errores);
				System.out.println("❌ Se encontraron errores (léxicos/sintácticos/semánticos). Revisa: " + errPath);
			} else {
				Files.writeString(errPath, "Sin errores detectados.\n");
				System.out.println("✅ Análisis completado sin errores.");
			}
		} catch (IOException e) {
			System.err.println("No se pudo escribir el archivo de errores: " + e.getMessage());
		}

		System.out.println("AST generado en: " + dotPath);
		System.out.println("Parse generado en: " + parsePath);
		System.out.println("Reglas aplicadas en: " + reglasPath);
	}
}
