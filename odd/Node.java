package odd;

import java.util.LinkedList;

public class Node {
	OddMove move;
	public double wins;
	public double visited;
	Node parent;
	LinkedList<Node> child;
	
	//Constructor for root Node.
	public Node() {
		wins = 0;
		visited = 0;
		move = null;
		parent = null;
	}
	
	public Node(OddMove move, Node parent){
		this.move = move;
		this.parent = parent;
		wins = 0;
		visited = 0;
	}
	
	public void addChild(Node ch) {
		child.add(ch);
	}
	
	public boolean isRoot(){
		if (parent == null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
