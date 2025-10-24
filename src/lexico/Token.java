package lexico;

/**
 * Representa un token léxico con su tipo, lexema y ubicación.
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int colStart;
    private final int colEnd;

    /**
     * @param type tipo del token (según TokenType)
     * @param lexeme texto asociado al token
     * @param line línea donde se encontró
     * @param colStart columna inicial
     * @param colEnd columna final
     */
    public Token(TokenType type, String lexeme, int line, int colStart, int colEnd) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.colStart = colStart;
        this.colEnd = colEnd;
    }

    @Override
    public String toString() {
        return "<" + type + ", " + lexeme + ">";
    }
}
