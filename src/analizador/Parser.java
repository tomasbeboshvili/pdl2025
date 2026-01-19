package analizador;

import java.util.ArrayList;
import java.util.List;
import analizador.SymbolTable.Type;
import analizador.SymbolTable.Category;

/**
 * Analizador sintáctico descendente para la gramática proporcionada.
 * Solo depende de ASTNode y Token (sin Symbol ni TokenType).
 */
public class Parser {
	private final List<Token> tokens;
	private int current = 0;
	private final ErrorManager errorManager;
	private final List<Integer> reglasAplicadas = new ArrayList<>();
	private final SymbolTable ts = new SymbolTable();

	public Parser(List<Token> tokens, ErrorManager errorManager) {
		this.tokens = tokens;
		this.errorManager = errorManager;
	}

	public ASTNode parseAST() {
		return P1();
	}

	public String getReglasAplicadasLinea() {
		StringBuilder sb = new StringBuilder("descendente");
		for (int r : reglasAplicadas) {
			sb.append(" ").append(r);
		}
		return sb.toString();
	}

	// 1: P1 -> P
	private ASTNode P1() {
		reglasAplicadas.add(1);
		ts.init();
		ASTNode node = new ASTNode("P1");
		node.addChild(P());
		return node;
	}

	// 2: P -> B P | 3: P -> F P | 4: P -> lambda
	private ASTNode P() {
		ASTNode node = new ASTNode("P");
		Type type;
		if (checkAny("PRfun")) {
			reglasAplicadas.add(3);
			ASTNode fNode = F();
			node.addChild(fNode);
			ASTNode pNode = P();
			node.addChild(pNode);
			type = (fNode.getSemanticType() == Type.OK && pNode.getSemanticType() == Type.OK) ? Type.OK : Type.ERROR;
		} else if (checkAny("PRlet", "PRif", "PRfor", "id", "PRwrite", "PRread", "PRreturn")) {
			reglasAplicadas.add(2);
			ASTNode bNode = B();
			node.addChild(bNode);
			ASTNode pNode = P();
			node.addChild(pNode);
			type = (bNode.getSemanticType() == Type.OK && pNode.getSemanticType() == Type.OK) ? Type.OK : Type.ERROR;
		} else {
			reglasAplicadas.add(4);
			type = Type.OK;
		}
		if (type == Type.ERROR) {
			// El error ya debería haber sido reportado en el punto de detección o se
			// reportará al usar el valor propgado
		}
		node.setSemanticType(type);
		return node;
	}

	// 5: B -> PRlet T id puntoComa | 6: B -> PRif parenIzq E parenDcha S | 7: B ->
	// PRfor ... | 8: B -> S
	private ASTNode B() {
		ASTNode node = new ASTNode("B");
		Type type = Type.ERROR;
		if (match("PRlet")) {
			reglasAplicadas.add(5);
			node.addChild(new ASTNode("PRlet"));
			ASTNode tNode = T();
			node.addChild(tNode);
			Token idTok = consume("id", "Se esperaba identificador");
			node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
			consume("puntoComa", "Se esperaba ';' tras declaración");

			if (!ts.existeLocal(idTok.getLexeme())) {
				ts.anadirVar(idTok.getLexeme(), tNode.getSemanticType());
				type = Type.OK;
			} else {
				errorSemantico(idTok, "Variable '" + idTok.getLexeme() + "' ya declarada");
				type = Type.ERROR;
			}
		} else if (match("PRif")) {
			reglasAplicadas.add(6);
			node.addChild(new ASTNode("PRif"));
			consume("parenIzq", "Se esperaba '(' tras if");
			ASTNode eNode = E();
			node.addChild(eNode);
			consume("parenDcha", "Se esperaba ')' tras la condición");
			ASTNode sNode = S();
			node.addChild(sNode);
			if (eNode.getSemanticType() == Type.BOOLEAN && sNode.getSemanticType() == Type.OK) {
				type = Type.OK;
			} else {
				if (eNode.getSemanticType() != Type.BOOLEAN) {
					errorSemantico(previous(), "La condición del 'if' debe ser boolean");
				}
				type = Type.ERROR;
			}
		} else if (match("PRfor")) {
			reglasAplicadas.add(7);
			node.addChild(new ASTNode("PRfor"));
			consume("parenIzq", "Se esperaba '(' tras for");
			ASTNode f1Init = F1();
			node.addChild(f1Init);
			consume("puntoComa", "Se esperaba ';' tras inicialización de for");
			ASTNode eNode = E();
			node.addChild(eNode);
			consume("puntoComa", "Se esperaba ';' tras condición de for");
			ASTNode f1Incr = F1();
			node.addChild(f1Incr);
			consume("parenDcha", "Se esperaba ')' tras incremento de for");
			consume("llaveIzq", "Se esperaba '{' tras cabecera de for");
			ASTNode cNode = C();
			node.addChild(cNode);
			consume("llaveDcha", "Se esperaba '}' al cerrar el for");
			// B -> PRfor parenIzq F1 puntoComa E puntoComa F1 parenDcha llaveIzq C
			// llaveDcha
			// { if (F1.tipo == ok AND E.tipo == boolean AND A.tipo == ok AND C.tipo == ok)
			// then B.tipo := ok else B.tipo := error }
			// Note: The rule mentions 'A.tipo == ok', but 'A' is not in the production.
			// Looking at the grammar provided: B -> PRfor parenIzq F1 puntoComa E puntoComa
			// F1 parenDcha llaveIzq C llaveDcha
			// The semantic action says: if (F1.tipo == ok AND E.tipo == boolean AND A.tipo
			// == ok AND C.tipo == ok)
			// I'll assume 'A' was a typo or refers to something else, maybe the first F1?
			// I'll use both F1s and C.
			if (f1Init.getSemanticType() == Type.OK && eNode.getSemanticType() == Type.BOOLEAN
					&& f1Incr.getSemanticType() == Type.OK && cNode.getSemanticType() == Type.OK) {
				type = Type.OK;
			} else {
				if (eNode.getSemanticType() != Type.BOOLEAN) {
					errorSemantico(previous(), "La condición del 'for' debe ser boolean");
				}
				type = Type.ERROR;
			}
		} else {
			reglasAplicadas.add(8);
			ASTNode sNode = S();
			node.addChild(sNode);
			type = sNode.getSemanticType();
		}
		node.setSemanticType(type);
		return node;
	}

	// 9: T -> PRint | 10: T -> PRfloat | 11: T -> PRboolean | 12: T -> PRstring
	private ASTNode T() {
		ASTNode node;
		Type type;
		if (match("PRint")) {
			reglasAplicadas.add(9);
			node = new ASTNode("PRint");
			type = Type.ENTERO;
		} else if (match("PRfloat")) {
			reglasAplicadas.add(10);
			node = new ASTNode("PRfloat");
			type = Type.REAL;
		} else if (match("PRboolean")) {
			reglasAplicadas.add(11);
			node = new ASTNode("PRboolean");
			type = Type.BOOLEAN;
		} else if (match("PRstring")) {
			reglasAplicadas.add(12);
			node = new ASTNode("PRstring");
			type = Type.CADENA;
		} else {
			error(peek(), "Tipo no válido"); // Sintáctico: token inesperado en lugar de tipo
			node = new ASTNode("tipo_error");
			type = Type.ERROR;
		}
		node.setSemanticType(type);
		return node;
	}

	// 13: F -> PRfun T id parenIzq Z parenDcha llaveIzq C F2 llaveDcha
	private ASTNode F() {
		reglasAplicadas.add(13);
		ASTNode node = new ASTNode("F");
		consume("PRfun", "Se esperaba 'function'");
		node.addChild(new ASTNode("PRfun"));
		ASTNode tNode = T();
		node.addChild(tNode);
		Token name = consume("id", "Se esperaba identificador de función");
		node.addChild(new ASTNode("id(" + name.getLexeme() + ")"));

		ts.anadirFunc(name.getLexeme(), tNode.getSemanticType());
		ts.entrarAmbito();

		consume("parenIzq", "Se esperaba '(' en la cabecera");
		ASTNode zNode = Z();
		node.addChild(zNode);
		consume("parenDcha", "Se esperaba ')' en la cabecera");

		ts.setParamsFunc(name.getLexeme(), zNode.getListaTipos());

		consume("llaveIzq", "Se esperaba '{' antes del cuerpo");
		ASTNode cNode = C();
		node.addChild(cNode);
		ASTNode f2Node = F2();
		node.addChild(f2Node);
		consume("llaveDcha", "Se esperaba '}' tras el cuerpo");

		Type type;
		if (f2Node.getSemanticType() == tNode.getSemanticType()) {
			type = Type.OK;
		} else {
			errorSemantico(name, "Retorno incorrecto en funcion " + name.getLexeme());
			type = Type.ERROR;
		}
		ts.salirAmbito();
		node.setSemanticType(type);
		return node;
	}

	// 14: F1 -> id W E | 15: F1 -> lambda
	private ASTNode F1() {
		ASTNode node = new ASTNode("F1");
		Type type;
		if (checkAny("id")) {
			reglasAplicadas.add(14);
			Token idTok = consume("id", "Se esperaba identificador");
			node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
			ASTNode wNode = W();
			node.addChild(wNode);
			ASTNode eNode = E();
			node.addChild(eNode);

			Type tId = ts.buscarTipo(idTok.getLexeme());
			if (tId == eNode.getSemanticType()) {
				type = Type.OK;
			} else {
				errorSemantico(idTok, "Tipos incompatibles en asignación: " + tId + " y " + eNode.getSemanticType());
				type = Type.ERROR;
			}
		} else {
			reglasAplicadas.add(15);
			type = Type.OK;
		}
		node.setSemanticType(type);
		return node;
	}

	// 16: F2 -> S1 | 17: F2 -> lambda
	private ASTNode F2() {
		ASTNode node = new ASTNode("F2");
		Type type;
		if (checkAny("PRreturn")) {
			reglasAplicadas.add(16);
			ASTNode s1Node = S1();
			node.addChild(s1Node);
			consume("puntoComa", "Se esperaba ';' tras return");
			type = s1Node.getSemanticType();
		} else {
			reglasAplicadas.add(17);
			type = Type.VOID;
		}
		node.setSemanticType(type);
		return node;
	}

	// 18: Z -> T id K | 19: Z -> lambda
	private ASTNode Z() {
		ASTNode node = new ASTNode("Z");
		List<Type> lista = new ArrayList<>();
		if (checkAny("PRint", "PRfloat", "PRboolean", "PRstring")) {
			reglasAplicadas.add(18);
			ASTNode tNode = T();
			node.addChild(tNode);
			Token idTok = consume("id", "Se esperaba identificador de parámetro");
			node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
			ASTNode kNode = K();
			node.addChild(kNode);

			if (!ts.existeLocal(idTok.getLexeme())) {
				ts.anadirVar(idTok.getLexeme(), tNode.getSemanticType());
				lista.add(tNode.getSemanticType());
				if (kNode.getListaTipos() != null) {
					lista.addAll(kNode.getListaTipos());
				}
			} else {
				// Error handled by returning null or empty? Specification says Z.listaTipos :=
				// error
				// I'll use null or a special list to represent error if needed.
			}
		} else {
			reglasAplicadas.add(19);
		}
		node.setListaTipos(lista);
		return node;
	}

	// 20: K -> coma T id K | 21: K -> lambda
	private ASTNode K() {
		ASTNode node = new ASTNode("K");
		List<Type> lista = new ArrayList<>();
		if (match("coma")) {
			reglasAplicadas.add(20);
			node.addChild(new ASTNode("coma"));
			ASTNode tNode = T();
			node.addChild(tNode);
			Token idTok = consume("id", "Se esperaba identificador de parámetro");
			node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
			ASTNode kNode = K();
			node.addChild(kNode);

			if (!ts.existeLocal(idTok.getLexeme())) {
				ts.anadirVar(idTok.getLexeme(), tNode.getSemanticType());
				lista.add(tNode.getSemanticType());
				if (kNode.getListaTipos() != null) {
					lista.addAll(kNode.getListaTipos());
				}
			}
		} else {
			reglasAplicadas.add(21);
		}
		node.setListaTipos(lista);
		return node;
	}

	// 22: E -> R E1
	private ASTNode E() {
		reglasAplicadas.add(22);
		ASTNode node = new ASTNode("E");
		ASTNode rNode = R();
		node.addChild(rNode);
		ASTNode e1Node = E1();
		node.addChild(e1Node);

		Type type;
		if (e1Node.getSemanticType() == null) {
			type = rNode.getSemanticType();
		} else if (rNode.getSemanticType() == Type.BOOLEAN && e1Node.getSemanticType() == Type.BOOLEAN) {
			type = Type.BOOLEAN;
		} else {
			type = Type.ERROR;
		}
		node.setSemanticType(type);
		return node;
	}

	// 23: E1 -> opAnd R E1 | 24: E1 -> lambda
	private ASTNode E1() {
		ASTNode node = new ASTNode("E1");
		Type type;
		if (match("opAnd")) {
			reglasAplicadas.add(23);
			node.addChild(new ASTNode("opAnd"));
			ASTNode rNode = R();
			node.addChild(rNode);
			ASTNode e1Node = E1();
			node.addChild(e1Node);

			if (rNode.getSemanticType() == Type.BOOLEAN
					&& (e1Node.getSemanticType() == Type.BOOLEAN || e1Node.getSemanticType() == null)) {
				type = Type.BOOLEAN;
			} else {
				type = Type.ERROR;
			}
		} else {
			reglasAplicadas.add(24);
			type = null;
		}
		node.setSemanticType(type);
		return node;
	}

	// 25: R -> U R1
	private ASTNode R() {
		reglasAplicadas.add(25);
		ASTNode node = new ASTNode("R");
		ASTNode uNode = U();
		node.addChild(uNode);
		ASTNode r1Node = R1();
		node.addChild(r1Node);

		Type type;
		if (r1Node.getSemanticType() == null) {
			type = uNode.getSemanticType();
		} else if (uNode.getSemanticType() == r1Node.getSemanticType()) {
			type = Type.BOOLEAN;
		} else {
			type = Type.ERROR;
		}
		node.setSemanticType(type);
		return node;
	}

	// 26: R1 -> opIgual U R1 | 27: R1 -> lambda
	private ASTNode R1() {
		ASTNode node = new ASTNode("R1");
		Type type;
		if (match("opIgual")) {
			reglasAplicadas.add(26);
			node.addChild(new ASTNode("opIgual"));
			ASTNode uNode = U();
			node.addChild(uNode);
			ASTNode r1Node = R1();
			node.addChild(r1Node);

			if (uNode.getSemanticType() == r1Node.getSemanticType() || r1Node.getSemanticType() == null) {
				type = uNode.getSemanticType();
			} else {
				type = Type.ERROR;
			}
		} else {
			reglasAplicadas.add(27);
			type = null;
		}
		node.setSemanticType(type);
		return node;
	}

	// 28: U -> V U1
	private ASTNode U() {
		reglasAplicadas.add(28);
		ASTNode node = new ASTNode("U");
		ASTNode vNode = V();
		node.addChild(vNode);
		ASTNode u1Node = U1();
		node.addChild(u1Node);

		Type type;
		if (u1Node.getSemanticType() == null) {
			type = vNode.getSemanticType();
		} else if (vNode.getSemanticType() == u1Node.getSemanticType()
				&& (vNode.getSemanticType() == Type.ENTERO || vNode.getSemanticType() == Type.REAL)) {
			type = vNode.getSemanticType();
		} else {
			type = Type.ERROR;
		}
		node.setSemanticType(type);
		return node;
	}

	// 29: U1 -> opSuma V U1 | 30: U1 -> lambda
	private ASTNode U1() {
		ASTNode node = new ASTNode("U1");
		Type type;
		if (match("opSuma")) {
			reglasAplicadas.add(29);
			node.addChild(new ASTNode("opSuma"));
			ASTNode vNode = V();
			node.addChild(vNode);
			ASTNode u1Node = U1();
			node.addChild(u1Node);

			if (vNode.getSemanticType() == u1Node.getSemanticType() || u1Node.getSemanticType() == null) {
				type = vNode.getSemanticType();
			} else {
				type = Type.ERROR;
			}
		} else {
			reglasAplicadas.add(30);
			type = null;
		}
		node.setSemanticType(type);
		return node;
	}

	// 31: V -> id V1 | 32: V -> parenIzq E parenDcha | 33: V -> entero | 34: V ->
	// real | 35: V -> cadena | 36: V -> true | 37: V -> false
	private ASTNode V() {
		ASTNode node = new ASTNode("V");
		Type type = Type.ERROR;
		if (match("id")) {
			reglasAplicadas.add(31);
			Token idTok = previous();
			node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));

			Type tipoId = ts.buscarTipo(idTok.getLexeme());
			Category catId = ts.buscarCategoria(idTok.getLexeme());

			ASTNode v1Node = V1(tipoId, catId, idTok.getLexeme());
			node.addChild(v1Node);
			type = v1Node.getSemanticType();
		} else if (match("parenIzq")) {
			reglasAplicadas.add(32);
			node.addChild(new ASTNode("parenIzq"));
			ASTNode eNode = E();
			node.addChild(eNode);
			consume("parenDcha", "Falta ')'");
			type = eNode.getSemanticType();
		} else if (match("entero")) {
			reglasAplicadas.add(33);
			node.addChild(new ASTNode("entero"));
			type = Type.ENTERO;
		} else if (match("real")) {
			reglasAplicadas.add(34);
			node.addChild(new ASTNode("real"));
			type = Type.REAL;
		} else if (match("cadena")) {
			reglasAplicadas.add(35);
			node.addChild(new ASTNode("cadena"));
			type = Type.CADENA;
		} else if (match("true")) {
			reglasAplicadas.add(36);
			node.addChild(new ASTNode("true"));
			type = Type.BOOLEAN;
		} else if (match("false")) {
			reglasAplicadas.add(37);
			node.addChild(new ASTNode("false"));
			type = Type.BOOLEAN;
		} else {
			error(peek(), "Expresión no válida"); // Sintáctico
		}
		node.setSemanticType(type);
		return node;
	}

	// 38: V1 -> parenIzq L parenDcha | 39: V1 -> lambda
	private ASTNode V1(Type h_tipoBase, Category h_categoria, String h_lexema) {
		ASTNode node = new ASTNode("V1");
		Type type;
		if (match("parenIzq")) {
			reglasAplicadas.add(38);
			node.addChild(new ASTNode("parenIzq"));
			ASTNode lNode = L();
			node.addChild(lNode);
			consume("parenDcha", "Falta ')'");

			if (h_categoria == Category.FUNCION) {
				List<Type> params = ts.buscarParams(h_lexema);
				if (params != null && params.equals(lNode.getListaTipos())) {
					type = h_tipoBase;
				} else {
					error(previous(), "Parámetros incorrectos en llamada a función '" + h_lexema + "'");
					type = Type.ERROR;
				}
			} else {
				error(previous(), "'" + h_lexema + "' no es una función");
				type = Type.ERROR;
			}
		} else {
			reglasAplicadas.add(39);
			if (h_categoria == Category.VARIABLE) {
				type = h_tipoBase;
			} else {
				error(previous(), "'" + h_lexema + "' no es una variable");
				type = Type.ERROR;
			}
		}
		node.setSemanticType(type);
		return node;
	}

	// 40: L -> E Q | 41: L -> lambda
	private ASTNode L() {
		ASTNode node = new ASTNode("L");
		List<Type> lista = new ArrayList<>();
		if (checkAny("id", "parenIzq", "entero", "real", "cadena", "true", "false")) {
			reglasAplicadas.add(40);
			ASTNode eNode = E();
			node.addChild(eNode);
			ASTNode qNode = Q();
			node.addChild(qNode);

			lista.add(eNode.getSemanticType());
			if (qNode.getListaTipos() != null) {
				lista.addAll(qNode.getListaTipos());
			}
		} else {
			reglasAplicadas.add(41);
		}
		node.setListaTipos(lista);
		return node;
	}

	// 42: Q -> coma E Q | 43: Q -> lambda
	private ASTNode Q() {
		ASTNode node = new ASTNode("Q");
		List<Type> lista = new ArrayList<>();
		if (match("coma")) {
			reglasAplicadas.add(42);
			node.addChild(new ASTNode("coma"));
			ASTNode eNode = E();
			node.addChild(eNode);
			ASTNode qNode = Q();
			node.addChild(qNode);

			lista.add(eNode.getSemanticType());
			if (qNode.getListaTipos() != null) {
				lista.addAll(qNode.getListaTipos());
			}
		} else {
			reglasAplicadas.add(43);
		}
		node.setListaTipos(lista);
		return node;
	}

	// 44: S -> id S2 | 45: S -> PRwrite E puntoComa | 46: S -> PRread id puntoComa
	// | 47: S -> S1 puntoComa
	private ASTNode S() {
		ASTNode node = new ASTNode("S");
		Type type = Type.ERROR;
		if (match("id")) {
			reglasAplicadas.add(44);
			Token idTok = previous();
			node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));

			Type tipoId = ts.buscarTipo(idTok.getLexeme());
			Category catId = ts.buscarCategoria(idTok.getLexeme());

			ASTNode s2Node = S2(tipoId, catId, idTok.getLexeme());
			node.addChild(s2Node);
			type = s2Node.getSemanticType();
		} else if (match("PRwrite")) {
			reglasAplicadas.add(45);
			node.addChild(new ASTNode("PRwrite"));
			ASTNode eNode = E();
			node.addChild(eNode);
			consume("puntoComa", "Se esperaba ';'");
			if (eNode.getSemanticType() != Type.ERROR) {
				type = Type.OK;
			} else {
				errorSemantico(previous(), "Error en expresión de 'write'");
				type = Type.ERROR;
			}
		} else if (match("PRread")) {
			reglasAplicadas.add(46);
			node.addChild(new ASTNode("PRread"));
			Token idTok = consume("id", "Se esperaba identificador en read");
			node.addChild(new ASTNode("id(" + idTok.getLexeme() + ")"));
			consume("puntoComa", "Se esperaba ';'");
			if (ts.existe(idTok.getLexeme())) {
				type = Type.OK;
			} else {
				errorSemantico(idTok, "Variable '" + idTok.getLexeme() + "' no declarada");
				type = Type.ERROR;
			}
		} else if (checkAny("PRreturn")) {
			reglasAplicadas.add(47);
			ASTNode s1Node = S1();
			node.addChild(s1Node);
			consume("puntoComa", "Se esperaba ';' tras return");
			type = s1Node.getSemanticType();
		} else {
			error(peek(), "Sentencia no válida"); // Sintáctico
		}
		node.setSemanticType(type);
		return node;
	}

	// 48: S1 -> PRreturn X
	private ASTNode S1() {
		reglasAplicadas.add(48);
		ASTNode node = new ASTNode("S1");
		consume("PRreturn", "Se esperaba 'return'");
		node.addChild(new ASTNode("PRreturn"));
		ASTNode xNode = X();
		node.addChild(xNode);
		node.setSemanticType(xNode.getSemanticType());
		return node;
	}

	// 49: S2 -> W E puntoComa | 50: S2 -> parenIzq L parenDcha puntoComa
	private ASTNode S2(Type h_tipoBase, Category h_categoria, String h_lexema) {
		ASTNode node = new ASTNode("S2");
		Type type = Type.ERROR;
		if (checkAny("igual", "asigDiv")) {
			reglasAplicadas.add(49);
			node.addChild(W());
			ASTNode eNode = E();
			node.addChild(eNode);
			consume("puntoComa", "Se esperaba ';'");

			if (h_categoria == Category.VARIABLE) {
				if (h_tipoBase == eNode.getSemanticType()) {
					type = Type.OK;
				} else {
					errorSemantico(previous(),
							"Tipos incompatibles en asignación: " + h_tipoBase + " y " + eNode.getSemanticType());
					type = Type.ERROR;
				}
			} else {
				errorSemantico(previous(), "'" + h_lexema + "' no es una variable");
				type = Type.ERROR;
			}
		} else if (match("parenIzq")) {
			reglasAplicadas.add(50);
			node.addChild(new ASTNode("parenIzq"));
			ASTNode lNode = L();
			node.addChild(lNode);
			consume("parenDcha", "Se esperaba ')'");
			consume("puntoComa", "Se esperaba ';'");

			if (h_categoria == Category.FUNCION) {
				List<Type> params = ts.buscarParams(h_lexema);
				if (params != null && params.equals(lNode.getListaTipos())) {
					type = Type.OK;
				} else {
					errorSemantico(previous(), "Parámetros incorrectos en llamada a función '" + h_lexema + "'");
					type = Type.ERROR;
				}
			} else {
				errorSemantico(previous(), "'" + h_lexema + "' no es una función");
				type = Type.ERROR;
			}
		} else {
			error(peek(), "Se esperaba asignación o llamada"); // Sintáctico
		}
		node.setSemanticType(type);
		return node;
	}

	// 51: W -> igual | 52: W -> asigDiv
	private ASTNode W() {
		ASTNode node = new ASTNode("W");
		if (match("igual")) {
			reglasAplicadas.add(51);
			node.addChild(new ASTNode("igual"));
		} else if (match("asigDiv")) {
			reglasAplicadas.add(52);
			node.addChild(new ASTNode("asigDiv"));
		} else {
			error(peek(), "Se esperaba '=' o '/='");
		}
		return node;
	}

	// 53: X -> E | 54: X -> lambda
	private ASTNode X() {
		ASTNode node = new ASTNode("X");
		Type type;
		if (checkAny("id", "parenIzq", "entero", "real", "cadena", "true", "false")) {
			reglasAplicadas.add(53);
			ASTNode eNode = E();
			node.addChild(eNode);
			type = eNode.getSemanticType();
		} else {
			reglasAplicadas.add(54);
			type = Type.VOID;
		}
		node.setSemanticType(type);
		return node;
	}

	// 55: C -> B C | 56: C -> lambda
	private ASTNode C() {
		ASTNode node = new ASTNode("C");
		Type type;
		if (checkAny("PRlet", "PRif", "PRfor", "id", "PRwrite", "PRread")) {
			reglasAplicadas.add(55);
			ASTNode bNode = B();
			node.addChild(bNode);
			ASTNode cNode = C();
			node.addChild(cNode);
			type = (bNode.getSemanticType() == Type.OK && cNode.getSemanticType() == Type.OK) ? Type.OK : Type.ERROR;
		} else {
			reglasAplicadas.add(56);
			type = Type.OK;
		}
		node.setSemanticType(type);
		return node;
	}

	public String getErrores() {
		return errorManager.getErroresString();
	}

	public boolean hasErrores() {
		return errorManager.hayErrores();
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
		if (isAtEnd())
			return false;
		String currentType = peek().getType();
		for (String type : types) {
			if (currentType.equals(type)) {
				return true;
			}
		}
		return false;
	}

	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}

	private boolean isAtEnd() {
		return "finFich".equals(peek().getType());
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private Token consume(String type, String message) {
		if (checkAny(type))
			return advance();

		error(peek(), message);

		// Recuperación inteligente:
		// Si esperábamos un ';' y nos encontramos un '}' o EOF, NO consumimos el token
		// actual.
		// Asumimos que el ';' falta y dejamos el '}' para que lo consuma la regla
		// superior (cierre de bloque).
		if (type.equals("puntoComa") && (checkAny("llaveDcha") || isAtEnd())) {
			return peek(); // Devolvemos el token actual sin avanzar
		}

		return advance(); // Comportamiento por defecto: consumir para intentar avanzar
	}

	// Error genérico (usado principalmente para sintácticos en consume)
	private void error(Token token, String message) {
		errorManager.agregarError("SINTÁCTICO", token.getLine(), message);
	}

	private void errorSemantico(Token token, String message) {
		errorManager.agregarError("SEMÁNTICO", token.getLine(), message);
	}

	public SymbolTable getTS() {
		return ts;
	}
}
