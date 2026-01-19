package analizador;

/**
 * Representa un token léxico con su tipo textual, lexema y ubicación.
 */
public class Token {
	private final String type;
	private final String lexeme;
	private final Integer symbolIndex; // opcional, solo para ids
	private final int line;
	private final int colStart;
	private final int colEnd;

	/**
	 * @param type        tipo lógico del token (por ejemplo "id", "int", "&&",
	 *                    "return"...)
	 * @param lexeme      texto asociado al token
	 * @param line        línea donde se encontró
	 * @param colStart    columna inicial
	 * @param colEnd      columna final
	 * @param symbolIndex posición del lexema en la tabla de símbolos (solo para
	 *                    ids, puede ser null)
	 */
	public Token(String type, String lexeme, int line, int colStart, int colEnd, Integer symbolIndex) {
		this.type = type;
		this.lexeme = lexeme;
		this.symbolIndex = symbolIndex;
		this.line = line;
		this.colStart = colStart;
		this.colEnd = colEnd;
	}

	public String getType() {
		return type;
	}

	public String getLexeme() {
		return lexeme;
	}

	public int getLine() {
		return line;
	}

	public int getColStart() {
		return colStart;
	}

	public int getColEnd() {
		return colEnd;
	}

	public boolean isType(String expected) {
		return type.equals(expected);
	}

	@Override
	public String toString() {
		if ("id".equals(type) && symbolIndex != null) {
			return "<" + lexeme + "," + symbolIndex + ">";
		}
		return "<" + type + ", " + lexeme + ">";
	}
}
