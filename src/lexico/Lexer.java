package lexico;

import java.util.*;

/**
 * Analizador léxico que devuelve tokens con tipos en texto
 * directamente alineados con la gramática del analizador sintáctico.
 */
public class Lexer {

    private final String input;                   // Código fuente de entrada
    private final List<Token> tokens = new ArrayList<>();  // Lista de tokens generados
    private final LinkedHashMap<String, Integer> symbolTable = new LinkedHashMap<>(); // Tabla de símbolos con posiciones
    private int pos = 0;                          // Posición actual en la cadena
    private int line = 1;                         // Línea actual
    private int column = 1;                       // Columna actual
    private int tokenStartColumn = 1;             // Columna donde comienza el token

    /** Palabras reservadas del lenguaje -> tipo de token resultante */
    private static final Map<String, String> keywords = new HashMap<>();

    static {
        keywords.put("boolean", "boolean");
        keywords.put("float", "float");
        keywords.put("for", "for");
        keywords.put("function", "function");
        keywords.put("if", "if");
        keywords.put("int", "int");
        keywords.put("let", "let");
        keywords.put("read", "read");
        keywords.put("return", "return");
        keywords.put("string", "string");
        keywords.put("void", "void");
        keywords.put("true", "true");
        keywords.put("false", "false");
        keywords.put("write", "write");
    }

    /**
     * Constructor del analizador léxico.
     * @param input texto fuente que se analizará.
     */
    public Lexer(String input) {
        this.input = input;
    }

    /**
     * Analiza todo el texto de entrada y genera los tokens correspondientes.
     * @return lista de tokens encontrados en el código fuente.
     */
    public List<Token> tokenize() {
        while (pos < input.length()) {
            char current = peek();

            if (current == '\n') {
                line++;
                column = 1;
                advance();
            } else if (Character.isWhitespace(current)) {
                advance();
            } else if (Character.isLetter(current) || current == '_') {
                tokenStartColumn = column;
                lexIdentifierOrKeyword();
            } else if (Character.isDigit(current)) {
                tokenStartColumn = column;
                lexNumber();
            } else {
                tokenStartColumn = column;
                switch (current) {
                    case '+':
                        advance();
                        if (match('+')) {
                            addToken("++", "++");
                        } else {
                            addToken("+", "+");
                        }
                        break;

                    case '=':
                        advance();
                        if (match('=')) addToken("==", "==");
                        else addToken("=", "=");
                        break;

                    case '&':
                        advance();
                        if (match('&')) addToken("&&", "&&");
                        else error("Símbolo no permitido '&'");
                        break;

					case '/':
						advance();
						if (match('=')) {
							addToken("/=", "/=");
						} else if (match('/')) {
							skipLineComment();  // Comentario de línea
						} else {
							error("Símbolo no permitido '/'");
						}
					break;


                    case ',':
                        advance(); addToken(",", ","); break;
                    case ';':
                        advance(); addToken(";", ";"); break;
                    case '(':
                        advance(); addToken("(", "("); break;
                    case ')':
                        advance(); addToken(")", ")"); break;
                    case '{':
                        advance(); addToken("{", "{"); break;
                    case '}':
                        advance(); addToken("}", "}"); break;
                    case '\'':
                        lexString();
                        break;
                    default:
                        error("Símbolo no reconocido: '" + current + "'");
                        advance();
                        break;
                }
            }
        }

        tokens.add(new Token("EOF", "", line, column, column, null));
        return tokens;
    }

    /**
     * Reconoce identificadores o palabras reservadas.
     * Si el lexema no está en las palabras clave, se agrega a la tabla de símbolos.
     */
    private void lexIdentifierOrKeyword() {
        int start = pos;
        while (pos < input.length() &&
                (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }
        String lexeme = input.substring(start, pos);
        String type = keywords.getOrDefault(lexeme, "id");
        if ("id".equals(type)) {
            int position = symbolTable.computeIfAbsent(lexeme, key -> symbolTable.size() + 1);
            addToken(type, lexeme, position);
        } else {
            addToken(type, lexeme);
        }
    }

    /**
     * Reconoce números enteros y reales (con punto decimal).
     * @throws NumberFormatException si el formato es inválido.
     */
    private void lexNumber() {
        int start = pos;
        boolean isReal = false;

        while (pos < input.length() && Character.isDigit(peek())) advance();

        if (peek() == '.') {
            isReal = true;
            advance();
            while (pos < input.length() && Character.isDigit(peek())) advance();
        }

        String lexeme = input.substring(start, pos);
        try {
            if (isReal) {
                double value = Double.parseDouble(lexeme);
                if (value > 117549436.0) error("Número real demasiado grande: " + lexeme);
                else addToken("real", lexeme);
            } else {
                int value = Integer.parseInt(lexeme);
                if (value > 32767) error("Número entero demasiado grande: " + lexeme);
                else addToken("ent", lexeme);
            }
        } catch (NumberFormatException e) {
            error("Número inválido: " + lexeme);
        }
    }

    /**
     * Reconoce cadenas de texto delimitadas por comillas simples ('texto').
     * Genera error si la cadena no se cierra.
     */
    private void lexString() {
        advance(); // Salta la comilla inicial
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && peek() != '\'') {
            if (peek() == '\n') line++;
            sb.append(peek());
            advance();
        }

        if (peek() == '\'') {
            advance();
            addToken("cad", sb.toString());
        } else {
            error("Cadena no cerrada");
        }
    }

    /**
     * Omite comentarios de bloque del tipo /* ... *\/.
     * Los ignora completamente sin generar tokens.
     */
    private void skipComment() {
        advance(); // salta '*'
        while (pos < input.length()) {
            if (peek() == '*' && peek(1) == '/') {
                advance(); advance();
                break;
            }
            if (peek() == '\n') line++;
            advance();
        }
    }

    /**
     * Añade un nuevo token a la lista.
     * @param type tipo de token textual
     * @param lexeme texto original del token
     */
    private void addToken(String type, String lexeme) {
        addToken(type, lexeme, null);
    }

    /**
     * Añade un token permitiendo indicar la posición en la tabla de símbolos.
     */
    private void addToken(String type, String lexeme, Integer symbolIndex) {
        int endCol = tokenStartColumn + lexeme.length() - 1;
        tokens.add(new Token(type, lexeme, line, tokenStartColumn, endCol, symbolIndex));
    }

    /**
     * Muestra un mensaje de error léxico en consola.
     * @param msg descripción del error.
     */
    private void error(String msg) {
        System.err.println("[ERROR - Línea " + line + "]: " + msg);
    }

    /** @return carácter actual sin avanzar el cursor. */
    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    /**
     * Lee un carácter adelantado sin mover la posición.
     * @param ahead cantidad de posiciones hacia adelante.
     * @return carácter leído o '\0' si está fuera de rango.
     */
    private char peek(int ahead) {
        return (pos + ahead) < input.length() ? input.charAt(pos + ahead) : '\0';
    }

    /** Avanza una posición en el texto fuente. */
    private void advance() {
        pos++;
        column++;
    }

    /**
     * Verifica si el siguiente carácter coincide con el esperado.
     * Si es así, avanza el cursor.
     * @param expected carácter esperado.
     * @return true si coincide, false si no.
     */
    private boolean match(char expected) {
        if (peek() == expected) {
            advance();
            return true;
        }
        return false;
    }

    /** @return tabla de símbolos generada durante el análisis. */
    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }

    /** @return representación en texto de la tabla de símbolos. */
    public String printSymbolTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tabla de Símbolos:\n");
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            sb.append(entry.getKey()).append("|").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

	/**
	 * Omite comentarios de línea del tipo // hasta el salto de línea o EOF.
	 */
	private void skipLineComment() {
		while (pos < input.length() && peek() != '\n') {
			advance();
		}
	}

}
