
package analizador;

import java.util.ArrayList;
import java.util.List;
import analizador.SymbolTable.Type;

public class ASTNode {
	private final String label;
	private final List<ASTNode> children = new ArrayList<>();
	private Type semanticType;
	private List<Type> listaTipos;

	public ASTNode(String label) {
		this.label = label;
	}

	public void addChild(ASTNode child) {
		children.add(child);
	}

	public String getLabel() {
		return label;
	}

	public List<ASTNode> getChildren() {
		return children;
	}

	public Type getSemanticType() {
		return semanticType;
	}

	public void setSemanticType(Type semanticType) {
		this.semanticType = semanticType;
	}

	public List<Type> getListaTipos() {
		return listaTipos;
	}

	public void setListaTipos(List<Type> listaTipos) {
		this.listaTipos = listaTipos;
	}

	public String toDotFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("digraph AST {\n");
		toDot(sb, new int[] { 0 });
		sb.append("}");
		return sb.toString();
	}

	/** Árbol en texto indentado para lectura rápida. */
	public String toIndentedString() {
		StringBuilder sb = new StringBuilder();
		toIndented(sb, 0);
		return sb.toString();
	}

	private void toIndented(StringBuilder sb, int level) {
		sb.append("  ".repeat(level)).append(label).append("\n");
		for (ASTNode child : children) {
			child.toIndented(sb, level + 1);
		}
	}

	private int toDot(StringBuilder sb, int[] id) {
		int myId = id[0]++;
		sb.append("n").append(myId).append(" [label=\"").append(label).append("\"];\n");
		for (ASTNode child : children) {
			int childId = child.toDot(sb, id);
			sb.append("n").append(myId).append(" -> n").append(childId).append(";\n");
		}
		return myId;
	}
}
