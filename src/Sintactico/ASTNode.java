
package Sintactico;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    private final String label;
    private final List<ASTNode> children = new ArrayList<>();

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

	
    public String toDotFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph AST {\n");
        toDot(sb, new int[]{0});
        sb.append("}");
        return sb.toString();
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
