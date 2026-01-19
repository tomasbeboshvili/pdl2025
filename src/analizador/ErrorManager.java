package analizador;

import java.util.ArrayList;
import java.util.List;

public class ErrorManager {
	private final List<String> errores = new ArrayList<>();
	private boolean hayErrores = false;

	public void agregarError(String tipo, int linea, String mensaje) {
		hayErrores = true;
		errores.add("[ERROR " + tipo + " - Línea " + linea + "]: " + mensaje);
	}

	public boolean hayErrores() {
		return hayErrores;
	}

	public List<String> getErrores() {
		return errores;
	}

	public String getErroresString() {
		StringBuilder sb = new StringBuilder();
		for (String err : errores) {
			sb.append(err).append("\n");
		}
		return sb.toString();
	}

	public void limpiar() {
		errores.clear();
		hayErrores = false;
	}
}
