package analizador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
	public enum Type {
		ENTERO, REAL, BOOLEAN, CADENA, VOID, ERROR, OK
	}

	public enum Category {
		VARIABLE, FUNCION
	}

	public static class SymbolInfo {
		public String lexeme;
		public Type type;
		public Category category;
		public List<Type> params;

		public SymbolInfo(String lexeme, Type type, Category category) {
			this.lexeme = lexeme;
			this.type = type;
			this.category = category;
			this.params = new ArrayList<>();
		}
	}

	private Stack<Map<String, SymbolInfo>> scopes;
	private Map<String, SymbolInfo> globalTable; // To keep track of functions globally if needed, or just use the stack

	public SymbolTable() {
		scopes = new Stack<>();
		globalTable = new HashMap<>();
		init();
	}

	public void init() {
		scopes.clear();
		globalTable.clear();
		scopes.push(new HashMap<>()); // Global scope
	}

	public void entrarAmbito() {
		scopes.push(new HashMap<>());
	}

	public void salirAmbito() {
		if (scopes.size() > 1) {
			scopes.pop();
		}
	}

	public boolean existeLocal(String id) {
		return scopes.peek().containsKey(id);
	}

	public boolean existe(String id) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(id)) {
				return true;
			}
		}
		return false;
	}

	public void anadirVar(String id, Type type) {
		SymbolInfo info = new SymbolInfo(id, type, Category.VARIABLE);
		scopes.peek().put(id, info);
	}

	public void anadirFunc(String id, Type type) {
		SymbolInfo info = new SymbolInfo(id, type, Category.FUNCION);
		// Functions are usually added to the global scope or the scope where they are
		// defined
		// According to rules, F -> PRfun T id ... TS.AnadirFunc(id.lex, T.tipo)
		scopes.peek().put(id, info);
		globalTable.put(id, info);
	}

	public void setParamsFunc(String id, List<Type> params) {
		SymbolInfo info = null;
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(id)) {
				info = scopes.get(i).get(id);
				break;
			}
		}
		if (info != null && info.category == Category.FUNCION) {
			info.params = new ArrayList<>(params);
		}
	}

	public Type buscarTipo(String id) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(id)) {
				return scopes.get(i).get(id).type;
			}
		}
		return Type.ERROR;
	}

	public Category buscarCategoria(String id) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(id)) {
				return scopes.get(i).get(id).category;
			}
		}
		return null;
	}

	public List<Type> buscarParams(String id) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(id)) {
				return scopes.get(i).get(id).params;
			}
		}
		return null;
	}
}
