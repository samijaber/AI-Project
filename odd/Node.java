package odd;

import java.util.LinkedList;

public class Node {
	OddMove move;
	double winrate;
	double visited;
	Node parent;
	LinkedList<Node> child;
	
	public Node(OddMove move, Node parent){
		this.move = move;
		this.parent = parent;
		winrate = 0;
		visited = 0;
	}
	
	public void addChild(Node ch) {
		child.add(ch);
	}
}
