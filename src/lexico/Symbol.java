package lexico;

/**
 * Representa una entrada de la tabla de símbolos
 * (identificadores encontrados durante el análisis léxico).
 */
public class Symbol {
    private final String lexema;

    /**
     * @param lexema nombre del identificador.
     */
    public Symbol(String lexema) {
        this.lexema = lexema;
    }

    /** @return el texto del identificador. */
    public String getLexema() {
        return lexema;
    }

    @Override
    public String toString() {
        return lexema;
    }
}
