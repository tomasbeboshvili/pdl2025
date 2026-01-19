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

		cases.add(new TestCase("caso01_declaraciones_tipos",
				"let int contador;\n" +
						"let float radio;\n" +
						"let float area;\n" +
						"let string nombre;\n" +
						"let boolean activo;\n" +
						"contador = 100;\n" +
						"radio = 5.5;\n" +
						"area = 0.0;\n" +
						"nombre = 'Juan';\n" +
						"activo = true;\n",
				"Correcto"));

		cases.add(new TestCase("caso02_funciones_nested",
				"function int cuadrado(int n) {\n" +
						"    return n + n; // Imaginamos que es n*n, pero solo tenemos + y comparadores\n" +
						"}\n" +
						"let int lado;\n" +
						"let int resultado;\n" +
						"lado = 4;\n" +
						"resultado = cuadrado(lado);\n",
				"Correcto"));

		cases.add(new TestCase("caso03_bucle_for_infinito",
				"let int i;\n" +
						"let int suma;\n" +
						"suma = 0;\n" +
						"// Bucle con condicion siempre true (valido semanticamente)\n" +
						"for (i = 0; true; i = i + 1) {\n" +
						"    suma = suma + i;\n" +
						"    if (i == 10) suma = 0;\n" +
						"}\n",
				"Correcto"));

		cases.add(new TestCase("caso04_condicional_complejo",
				"let int puntaje;\n" +
						"let boolean pasa;\n" +
						"puntaje = 60;\n" +
						"pasa = false;\n" +
						"// Usamos == y && que si estan soportados en lexico y gramatica\n" +
						"if (puntaje == 60 && true) pasa = true;\n",
				"Correcto"));

		cases.add(new TestCase("caso05_operaciones_variadas",
				"let float x;\n" +
						"let float y;\n" +
						"x = 10.0;\n" +
						"y = 2.0;\n" +
						"x /= 2.0;\n" +
						"// Solo tenemos + y /=\n" +
						"x = x + y;\n" +
						"x /= y;\n",
				"Correcto"));

		// --- CASOS INCORRECTOS ---

		cases.add(new TestCase("caso06_error_lexico_simbolos",
				"let int usuario;\n" +
						"usuario = 1;\n" +
						"// @ y # no estan permitidos\n" +
						"if (usuario == 1) usuario = 2; @ \n",
				"Incorrecto"));

		cases.add(new TestCase("caso07_error_sintactico_punto_coma",
				"let string mensaje;\n" +
						"mensaje = 'Hola' // Falta punto y coma\n" +
						"let int largo;\n" +
						"largo = 4;\n",
				"Incorrecto"));

		cases.add(new TestCase("caso08_error_sintactico_parentesis",
				"let int a;\n" +
						"a = 10;\n" +
						"// Falta parentesis de cierre\n" +
						"if (a == 10 a = 0;\n",
				"Incorrecto"));

		cases.add(new TestCase("caso09_error_semantico_tipos_incompatibles",
				"let int edad;\n" +
						"let string texto;\n" +
						"edad = 25;\n" +
						"texto = 'Veinticinco';\n" +
						"edad = texto; // Error: asignar string a int\n",
				"Incorrecto"));

		cases.add(new TestCase("caso10_error_semantico_no_declarada_y_params",
				"let int x;\n" +
						"let int y;\n" +
						"x = 5;\n" +
						"y = 10;\n" +
						"\n" +
						"function int doble(int n) {\n" +
						"    return n + n;\n" +
						"}\n" +
						"x = doble(true); // Parametro boolean en lugar de int\n" +
						"z = 5; // z no declarada\n",
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
		try {
			parser.parseAST();
		} catch (Exception e) {
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

		// 3. Tabla de Símbolos
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
			output.append("\n\n\n");
		}

		try (BufferedWriter writer = Files.newBufferedWriter(outDir.resolve(tc.name + ".txt"))) {
			writer.write(output.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
