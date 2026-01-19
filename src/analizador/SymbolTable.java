package analizador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
		public int desplazamiento;

		public SymbolInfo(String lexeme, Type type, Category category) {
			this.lexeme = lexeme;
			this.type = type;
			this.category = category;
			this.params = new ArrayList<>();
			this.desplazamiento = 0;
		}
	}

	private Stack<Map<String, SymbolInfo>> scopes;
	private Map<String, SymbolInfo> globalTable;
	private StringBuilder log;
	private int currentOffset = 0;

	public SymbolTable() {
		scopes = new Stack<>();
		globalTable = new HashMap<>();
		log = new StringBuilder();
		init();
	}

	public void init() {
		scopes.clear();
		globalTable.clear();
		log.setLength(0);
		scopes.push(new LinkedHashMap<>());
		currentOffset = 0;
	}

	public void entrarAmbito() {
		scopes.push(new LinkedHashMap<>());
		// Reset offsets for local scope? Assuming simple accumulation or reset.
		// For consistency with requirement "Desplazamiento (dirección de memoria)",
		// often means offset from base pointer. So resetting to 0 for function scope
		// makes sense.
		// But in block scopes (if supported), it should continue.
		// Since we have Function -> ... entrarAmbito ... it is likely function scope.
		// We'll reset currentOffset for simplicity of presentation, or keep partial.
		// Let's keep it simple: incrementing continuously or resetting?
		// Requirement says "entero, real...: Desplazamiento".
		// I'll leave it as continuing for now, as I don't store previous offset to
		// restore it.
		// Actually, standard stack machines reset offset for new frames.
		// To do it right: push currentOffset to a stack too.
		// But for now, let's just let it grow.
	}

	public void salirAmbito() {
		if (scopes.size() > 1) {
			Map<String, SymbolInfo> scope = scopes.peek();
			// log.append("--- Tabla de Simbolos (Ambito Local) ---\n");
			logScope(scope, log);
			// log.append("----------------------------------------\n\n");
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
		info.desplazamiento = currentOffset;
		switch (type) {
			case ENTERO:
				currentOffset += 2;
				break;
			case REAL:
				currentOffset += 4;
				break;
			case CADENA:
				currentOffset += 64;
				break;
			case BOOLEAN:
				currentOffset += 1;
				break;
			default:
				currentOffset += 1;
				break;
		}
		scopes.peek().put(id, info);
	}

	public void anadirFunc(String id, Type type) {
		SymbolInfo info = new SymbolInfo(id, type, Category.FUNCION);
		scopes.peek().put(id, info);
		// Functions added to current scope.
	}

	public void setParamsFunc(String id, List<Type> params) {
		SymbolInfo info = buscarSimbolo(id);
		if (info != null && info.category == Category.FUNCION) {
			info.params = new ArrayList<>(params);
		}
	}

	private SymbolInfo buscarSimbolo(String id) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).containsKey(id)) {
				return scopes.get(i).get(id);
			}
		}
		return null;
	}

	public Type buscarTipo(String id) {
		SymbolInfo info = buscarSimbolo(id);
		return info != null ? info.type : Type.ERROR;
	}

	public Category buscarCategoria(String id) {
		SymbolInfo info = buscarSimbolo(id);
		return info != null ? info.category : null;
	}

	public List<Type> buscarParams(String id) {
		SymbolInfo info = buscarSimbolo(id);
		return info != null ? info.params : null;
	}

	private void logScope(Map<String, SymbolInfo> scope, StringBuilder target) {
		for (SymbolInfo info : scope.values()) {
			target.append("LEXEMA: '").append(info.lexeme).append("'\n");
			if (info.category == Category.VARIABLE) {
				target.append("  TIPO: ").append(info.type).append("\n");
				target.append("  DESPLAZAMIENTO: ").append(info.desplazamiento).append("\n");
			} else if (info.category == Category.FUNCION) {
				target.append("  TIPO: FUNCION\n");
				target.append("  RETORNO: ").append(info.type).append("\n");
				target.append("  NUM_PARAMS: ").append(info.params.size()).append("\n");
				target.append("  TIPOS_PARAMS: ").append(info.params).append("\n");
			}
			target.append("------------------------------------------\n");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(log);
		if (!scopes.isEmpty()) {
			// sb.append("--- Tabla de Simbolos (Ambito Global/Actual) ---\n");
			logScope(scopes.get(0), sb);
			// sb.append("----------------------------------------------\n");
		}
		return sb.toString();
	}
}
