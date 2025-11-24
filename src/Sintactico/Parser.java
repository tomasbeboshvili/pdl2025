package Sintactico;

import java.util.ArrayList;
import java.util.List;

import lexico.Token;

/**
 * Analizador sintáctico descendente para la gramática proporcionada.
 * Solo depende de ASTNode y Token (sin Symbol ni TokenType).
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final StringBuilder errores = new StringBuilder();
    private boolean hayErrores = false;
    private final List<Integer> reglasAplicadas = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parseAST() {
        ASTNode root = new ASTNode("P");
        root.addChild(P());
        return root;
    }

    public String getReglasAplicadasLinea() {
        StringBuilder sb = new StringBuilder("descendente");
        for (int r : reglasAplicadas) {
            sb.append(" ").append(r);
        }
        return sb.toString();
    }

    // P -> B P | F P | λ
    private ASTNode P() {
        ASTNode node = new ASTNode("P");
        if (checkAny("function")) {
            reglasAplicadas.add(2); // P -> F P
            node.addChild(F());
            node.addChild(P());
        } else if (checkAny("let", "if", "for", "id", "write", "read", "return")) {
            reglasAplicadas.add(1); // P -> B P
            node.addChild(B());
            node.addChild(P());
        } else {
            reglasAplicadas.add(3); // P -> lambda
        }
        return node;
    }

    // B -> let T id ; | if ( E ) S | S | for ( F1 ; E ; A ) { C }
    private ASTNode B() {
        ASTNode node = new ASTNode("B");
        if (match("let")) {
            reglasAplicadas.add(4);
            node.addChild(new ASTNode("let"));
            node.addChild(T());
            Token idTok = consume("id", "Se esperaba identificador");
            node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
            consume(";", "Falta ';' tras declaración");
        } else if (match("if")) {
            reglasAplicadas.add(5);
            node.addChild(new ASTNode("if"));
            consume("(", "Se esperaba '(' tras if");
            node.addChild(E());
            consume(")", "Se esperaba ')' tras la condición");
            node.addChild(S());
        } else if (match("for")) {
            reglasAplicadas.add(6);
            node.addChild(new ASTNode("for"));
            consume("(", "Se esperaba '(' tras for");
            node.addChild(F1());
            consume(";", "Falta ';' tras inicialización de for");
            node.addChild(E());
            consume(";", "Falta ';' tras condición de for");
            node.addChild(A());
            consume(")", "Falta ')' tras incremento de for");
            consume("{", "Se esperaba '{' tras cabecera de for");
            node.addChild(C());
            consume("}", "Se esperaba '}' al cerrar el for");
        } else {
            reglasAplicadas.add(7);
            node.addChild(S());
        }
        return node;
    }

    // F -> function H id ( Z ) { C F5 }
    private ASTNode F() {
        reglasAplicadas.add(8);
        ASTNode node = new ASTNode("F");
        consume("function", "Se esperaba 'function'");
        node.addChild(new ASTNode("function"));
        node.addChild(H());
        Token name = consume("id", "Se esperaba identificador de función");
        node.addChild(new ASTNode("id(" + name.getLexeme() + ")"));
        consume("(", "Falta '(' en la cabecera");
        node.addChild(Z());
        consume(")", "Falta ')' en la cabecera");
        consume("{", "Falta '{' antes del cuerpo");
        node.addChild(C());
        node.addChild(F5());
        consume("}", "Falta '}' tras el cuerpo");
        return node;
    }

    // F5 -> S1 | λ
    private ASTNode F5() {
        ASTNode node = new ASTNode("F5");
        if (checkAny("return")) {
            reglasAplicadas.add(9);
            node.addChild(S1());
        } else {
            reglasAplicadas.add(10);
        }
        return node;
    }

    // H -> T | void
    private ASTNode H() {
        if (checkAny("int", "float", "boolean", "string", "void")) {
            reglasAplicadas.add(11);
            return T();
        }
        error(peek(), "Se esperaba tipo o void");
        return new ASTNode("tipo_error");
    }

    // T -> int | float | boolean | string | void
    private ASTNode T() {
        if (match("int")) { reglasAplicadas.add(12); return new ASTNode("int"); }
        if (match("float")) { reglasAplicadas.add(13); return new ASTNode("float"); }
        if (match("boolean")) { reglasAplicadas.add(14); return new ASTNode("boolean"); }
        if (match("string")) { reglasAplicadas.add(15); return new ASTNode("string"); }
        if (match("void")) { reglasAplicadas.add(16); return new ASTNode("void"); }
        error(peek(), "Tipo no válido");
        return new ASTNode("tipo_error");
    }

    // Z -> T id K | λ
    private ASTNode Z() {
        ASTNode node = new ASTNode("Z");
        if (checkAny("int", "float", "boolean", "string", "void")) {
            reglasAplicadas.add(17);
            node.addChild(T());
            Token idTok = consume("id", "Se esperaba identificador de parámetro");
            node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
            node.addChild(K());
        } else {
            reglasAplicadas.add(18);
        }
        return node;
    }

    // K -> , T id K | λ
    private ASTNode K() {
        ASTNode node = new ASTNode("K");
        if (match(",")) {
            reglasAplicadas.add(19);
            node.addChild(new ASTNode(","));
            node.addChild(T());
            Token idTok = consume("id", "Se esperaba identificador de parámetro");
            node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
            node.addChild(K());
        } else {
            reglasAplicadas.add(20);
        }
        return node;
    }

    // E -> R E1
    private ASTNode E() {
        reglasAplicadas.add(21);
        ASTNode node = new ASTNode("E");
        node.addChild(R());
        node.addChild(E1());
        return node;
    }

    // E1 -> && R E1 | λ
    private ASTNode E1() {
        ASTNode node = new ASTNode("E1");
        if (match("&&")) {
            reglasAplicadas.add(22);
            node.addChild(new ASTNode("&&"));
            node.addChild(R());
            node.addChild(E1());
        } else {
            reglasAplicadas.add(23);
        }
        return node;
    }

    // R -> U R1
    private ASTNode R() {
        reglasAplicadas.add(24);
        ASTNode node = new ASTNode("R");
        node.addChild(U());
        node.addChild(R1());
        return node;
    }

    // R1 -> == U R1 | λ
    private ASTNode R1() {
        ASTNode node = new ASTNode("R1");
        if (match("==")) {
            reglasAplicadas.add(25);
            node.addChild(new ASTNode("=="));
            node.addChild(U());
            node.addChild(R1());
        } else {
            reglasAplicadas.add(26);
        }
        return node;
    }

    // U -> V U1
    private ASTNode U() {
        reglasAplicadas.add(27);
        ASTNode node = new ASTNode("U");
        node.addChild(V());
        node.addChild(U1());
        return node;
    }

    // U1 -> + V U1 | λ
    private ASTNode U1() {
        ASTNode node = new ASTNode("U1");
        if (match("+")) {
            reglasAplicadas.add(28);
            node.addChild(new ASTNode("+"));
            node.addChild(V());
            node.addChild(U1());
        } else {
            reglasAplicadas.add(29);
        }
        return node;
    }

    // V -> id | id ( L ) | ( E ) | ent | cad | true | false | real
    private ASTNode V() {
        ASTNode node = new ASTNode("V");
        if (match("id")) {
            Token idTok = previous();
            if (match("(")) {
                reglasAplicadas.add(31);
                node.addChild(new ASTNode("call(" + idTok.getLexeme() + ")"));
                node.addChild(L());
                consume(")", "Falta ')' tras los argumentos");
            } else {
                reglasAplicadas.add(30);
                node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
            }
        } else if (match("(")) {
            reglasAplicadas.add(32);
            node.addChild(new ASTNode("("));
            node.addChild(E());
            consume(")", "Falta ')'");
        } else if (match("ent")) {
            reglasAplicadas.add(33);
            node.addChild(new ASTNode("ent"));
        } else if (match("real")) {
            reglasAplicadas.add(34);
            node.addChild(new ASTNode("real"));
        } else if (match("cad")) {
            reglasAplicadas.add(35);
            node.addChild(new ASTNode("cad"));
        } else if (match("true")) {
            reglasAplicadas.add(36);
            node.addChild(new ASTNode("true"));
        } else if (match("false")) {
            reglasAplicadas.add(37);
            node.addChild(new ASTNode("false"));
        } else {
            error(peek(), "Expresión no válida");
        }
        return node;
    }

    // L -> E Q | λ
    private ASTNode L() {
        ASTNode node = new ASTNode("L");
        if (checkAny("id", "ent", "real", "cad", "true", "false", "(")) {
            reglasAplicadas.add(38);
            node.addChild(E());
            node.addChild(Q());
        } else {
            reglasAplicadas.add(39);
        }
        return node;
    }

    // Q -> , E Q | λ
    private ASTNode Q() {
        ASTNode node = new ASTNode("Q");
        if (match(",")) {
            reglasAplicadas.add(40);
            node.addChild(new ASTNode(","));
            node.addChild(E());
            node.addChild(Q());
        } else {
            reglasAplicadas.add(41);
        }
        return node;
    }

    // S -> id W E ; | id ( L ) ; | write E ; | read id ; | S1 ;
    private ASTNode S() {
        ASTNode node = new ASTNode("S");
        if (match("id")) {
            Token idTok = previous();
            if (match("(")) {
                reglasAplicadas.add(43);
                node.addChild(new ASTNode("call(" + idTok.getLexeme() + ")"));
                node.addChild(L());
                consume(")", "Falta ')' en la llamada");
                consume(";", "Falta ';'");
            } else {
                reglasAplicadas.add(42);
                node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
                node.addChild(W());
                node.addChild(E());
                consume(";", "Falta ';'");
            }
        } else if (match("write")) {
            reglasAplicadas.add(44);
            node.addChild(new ASTNode("write"));
            node.addChild(E());
            consume(";", "Falta ';'");
        } else if (match("read")) {
            reglasAplicadas.add(45);
            node.addChild(new ASTNode("read"));
            Token idTok = consume("id", "Se esperaba identificador en read");
            node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
            consume(";", "Falta ';'");
        } else if (checkAny("return")) {
            reglasAplicadas.add(46);
            node.addChild(S1());
            consume(";", "Falta ';' tras return");
        } else {
            error(peek(), "Sentencia no válida");
        }
        return node;
    }

    // S1 -> return X
    private ASTNode S1() {
        reglasAplicadas.add(47);
        ASTNode node = new ASTNode("S1");
        consume("return", "Falta 'return'");
        node.addChild(new ASTNode("return"));
        node.addChild(X());
        return node;
    }

    // W -> = | /=
    private ASTNode W() {
        ASTNode node = new ASTNode("W");
        if (match("=")) {
            reglasAplicadas.add(48);
            node.addChild(new ASTNode("="));
        } else if (match("/=")) {
            reglasAplicadas.add(49);
            node.addChild(new ASTNode("/="));
        } else {
            error(peek(), "Se esperaba '=' o '/='");
        }
        return node;
    }

    // X -> E | λ
    private ASTNode X() {
        ASTNode node = new ASTNode("X");
        if (!checkAny(";")) {
            reglasAplicadas.add(50);
            node.addChild(E());
        } else {
            reglasAplicadas.add(51);
        }
        return node;
    }

    // F1 -> id W E | λ
    private ASTNode F1() {
        ASTNode node = new ASTNode("F1");
        if (checkAny("id") && checkNextAny("=", "/=")) {
            reglasAplicadas.add(52);
            Token idTok = consume("id", "Se esperaba identificador");
            node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
            node.addChild(W());
            node.addChild(E());
        } else {
            reglasAplicadas.add(53);
        }
        return node;
    }

    // A -> F1 | id++ | λ
    private ASTNode A() {
        ASTNode node = new ASTNode("A");
        if (checkAny("id") && checkNextAny("=", "/=")) {
            reglasAplicadas.add(54);
            node.addChild(F1());
        } else if (checkAny("id") && checkNextAny("++")) {
            reglasAplicadas.add(55);
            Token idTok = consume("id", "Se esperaba identificador");
            node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
            consume("++", "Se esperaba '++'");
            node.addChild(new ASTNode("++"));
        } else {
            reglasAplicadas.add(56);
        }
        return node;
    }

    // C -> B C | λ
    private ASTNode C() {
        ASTNode node = new ASTNode("C");
        if (checkAny("let", "if", "for", "id", "write", "read", "return")) {
            reglasAplicadas.add(57);
            node.addChild(B());
            node.addChild(C());
        } else {
            reglasAplicadas.add(58);
        }
        return node;
    }

    public String getErrores() {
        return errores.toString();
    }

    public boolean hasErrores() {
        return hayErrores;
    }

    private boolean match(String... types) {
        for (String type : types) {
            if (checkAny(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean checkAny(String... types) {
        if (isAtEnd()) return false;
        String currentType = peek().getType();
        for (String type : types) {
            if (currentType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNextAny(String... types) {
        if (current + 1 >= tokens.size()) return false;
        String nextType = tokens.get(current + 1).getType();
        for (String type : types) {
            if (nextType.equals(type)) return true;
        }
        return false;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return "EOF".equals(peek().getType());
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(String type, String message) {
        if (checkAny(type)) return advance();
        error(peek(), message);
        return advance();
    }

    private void error(Token token, String message) {
        hayErrores = true;
        errores.append("[ERROR - Línea ").append(token.getLine()).append("]: ").append(message).append("\n");
    }
}
