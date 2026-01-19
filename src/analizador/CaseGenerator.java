package analizador;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CaseGenerator {

	static class TestCase {
		String name;
		String code;
		String type; // "Correcto" o "Incorrecto"

		public TestCase(String name, String code, String type) {
			this.name = name;
			this.code = code;
			this.type = type;
		}
	}

	public static void main(String[] args) {
		List<TestCase> cases = new ArrayList<>();

		// --- CASOS CORRECTOS ---

		cases.add(new TestCase("caso01_declaraciones",
				"let int a;\n" +
						"let float b;\n" +
						"let string s;\n" +
						"a = 10;\n" +
						"b = 3.14;\n" +
						"s = 'hola';\n",
				"Correcto"));

		cases.add(new TestCase("caso02_funcion_llamada",
				"function int suma(int x) {\n" +
						"    return x + 1;\n" +
						"}\n" +
						"let int res;\n" +
						"res = suma(5);\n",
				"Correcto"));

		cases.add(new TestCase("caso03_bucle_for",
				"let int i;\n" +
						"let int total;\n" +
						"total = 0;\n" +
						"for (i = 0; i < 10; i = i + 1) {\n" +
						"    total = total + i;\n" +
						"}\n",
				"Correcto"));

		cases.add(new TestCase("caso04_condicional",
				"let int x;\n" +
						"x = 5;\n" +
						"if (x > 0 && x < 10) {\n" +
						"    x = 0;\n" +
						"}\n",
				"Correcto"));

		cases.add(new TestCase("caso05_asignacion_compuesta",
				"let float x;\n" +
						"x = 20.0;\n" +
						"x /= 2.0;\n" +
						"x = x + 1.5;\n",
				"Correcto"));

		// --- CASOS INCORRECTOS ---

		cases.add(new TestCase("caso06_error_lexico",
				"let int @var;\n" +
						"let int j#;\n",
				"Incorrecto"));

		cases.add(new TestCase("caso07_error_sintactico_punto_coma",
				"let int a\n" +
						"a = 5;\n",
				"Incorrecto"));

		cases.add(new TestCase("caso08_error_sintactico_parentesis",
				"if (5 > 3 {\n" +
						"    let int x;\n" +
						"}\n",
				"Incorrecto"));

		cases.add(new TestCase("caso09_error_semantico_tipos",
				"let int a;\n" +
						"a = 'texto';\n",
				"Incorrecto"));

		cases.add(new TestCase("caso10_error_semantico_no_declarada",
				"a = 5;\n",
				"Incorrecto"));

		// --- EJECUCIÓN ---

		Path outDir = Paths.get("casos_entrega");
		try {
			Files.createDirectories(outDir);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (TestCase tc : cases) {
			procesarCaso(tc, outDir);
		}
		System.out.println("Generación de casos completada en: " + outDir.toAbsolutePath());
	}

	private static void procesarCaso(TestCase tc, Path outDir) {
		System.out.println("Procesando: " + tc.name);

		ErrorManager errorManager = new ErrorManager();
		Lexer lexer = new Lexer(tc.code, errorManager);
		List<Token> tokens = lexer.tokenize();

		Parser parser = new Parser(tokens, errorManager);
		// Ejecutamos análisis solo si no hay errores léxicos graves que impidan parsear
		// aunque tu lexer siempre devuelve tokens.
		try {
			parser.parseAST();
		} catch (Exception e) {
			// En caso de panic mode fallido (raro), capturamos.
		}

		StringBuilder output = new StringBuilder();

		// 1. Título y Código
		output.append("Caso de Prueba: ").append(tc.name).append(" (").append(tc.type).append(")\n\n");
		output.append("Código :\n");
		output.append(tc.code).append("\n");
		output.append("------------------------------\n");

		// 2. Tokens
		output.append("Tokens\n");
		for (Token t : tokens) {
			output.append(t.toString()).append("\n");
		}
		output.append("------------------------------\n");

		// 3. Tabla de Símbolos (siempre la imprimimos, incluso con errores puede haber
		// parciales)
		output.append("tabla de simbolos\n");
		output.append(parser.getTS().toString()).append("\n");
		output.append("------------------------------\n");

		// 4. Reglas VAST o Errores
		if (errorManager.hayErrores()) {
			output.append("Errores Detectados\n");
			output.append(errorManager.getErroresString()).append("\n");
			output.append("------------------------------\n");
			output.append("Arbol generado\n");
			output.append("(No se genera árbol debido a errores)\n");
		} else {
			output.append("Reglas para VAST\n");
			output.append(parser.getReglasAplicadasLinea()).append("\n");
			output.append("------------------------------\n");
			output.append("Arbol generado\n");
			// Espacio para pegar la imagen/texto del árbol manual
			output.append("\n\n\n");
		}

		// Escribir a fichero
		try (BufferedWriter writer = Files.newBufferedWriter(outDir.resolve(tc.name + ".txt"))) {
			writer.write(output.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
