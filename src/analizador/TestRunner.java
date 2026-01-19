package analizador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestRunner {
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		String[] tests = {
				"test1_declaraciones",
				"test2_bucle_for",
				"test3_condicional_if",
				"test4_funcion_llamada",
				"sem_ok",
				"sem_err_redecl",
				"sem_err_type",
				"sem_err_return",
				"sem_err_params",
				"sem_err_cond"
		};

		Path testsDir = Paths.get("src", "analizador", "tests");

		for (String test : tests) {
			Path testPath = testsDir.resolve(test);
			Path inputPath = testPath.resolve("input.js");

			if (!Files.exists(inputPath))
				continue;

			String source = Files.readString(inputPath);
			ErrorManager errorManager = new ErrorManager();
			Lexer lexer = new Lexer(source, errorManager);
			List<Token> tokens = lexer.tokenize();
			Parser parser = new Parser(tokens, errorManager);
			ASTNode ast = parser.parseAST();

			// Files.writeString(testPath.resolve("ast.dot"), ast.toDotFile()); // AST
			// Visual eliminado
			// Files.writeString(testPath.resolve("arbol_debug.txt"),
			// ast.toIndentedString()); // Árbol debug eliminado

			// El parse de reglas es lo que se entrega como parse.txt
			Files.writeString(testPath.resolve("parse.txt"), parser.getReglasAplicadasLinea());

			if (parser.hasErrores()) {
				Files.writeString(testPath.resolve("errores.txt"), parser.getErrores());
			} else {
				Files.deleteIfExists(testPath.resolve("errores.txt"));
			}

			System.out.println("Procesado: " + test);
		}
	}
}
