package lexico;

/**
 * Enum que define los tipos de tokens reconocidos por el analizador léxico.
 */
public enum TokenType {
    // Palabras reservadas
    PRboolean, PRfloat, PRfor, PRfun, PRif, PRint, PRlet, PRread, PRreturn, PRstring,
    trueConst, falseConst,

    // Tipos de constantes e identificadores
    id, entero, real, cadena,

    // Operadores
    igual, opIgual, opSuma, opAnd, asigDiv,

    // Símbolos de puntuación
    coma, puntoComa, parenIzq, parenDcha, llaveIzq, llaveDcha,

    // Fin de fichero
    finFich
}
