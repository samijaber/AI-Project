package odd;

import java.util.LinkedList;

public class Node {
	OddMove move;
	public double wins;
	public double visited;
	Node parent;
	LinkedList<Node> child;
	public double constant = 1;
	
	//Constructor for root Node.
	public Node() {
		wins = 0;
		visited = 0;
		move = null;
		parent = null;
		child = new LinkedList<Node>();
	}
	
	public Node(OddMove move, Node parent){
		this.move = move;
		this.parent = parent;
		wins = 0;
		visited = 0;
		child = new LinkedList<Node>();
		
		//add this as a child of its parent
		this.parent.addChild(this);
	}
	
	public Node hasChild(OddMove move) {

		for (Node n: child)
		{
			if ((n.move.destCol == move.destCol) && (n.move.destRow == move.destRow))
			{
				if(n.move.color == move.color)
					return n;
			}
		}
		return null;
	}
	
	public double getwinRate(){
		return wins/visited;
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
	
	public void addWin(){
		wins++;
	}
	
	public double UCB(){
		double exploration = 0;
		if (parent.visited > 0)
		{
			exploration = Math.sqrt(Math.log(parent.visited));
		}
		if (visited > 0)
		{
			exploration = exploration/Math.sqrt(visited);
		}
		
		return wins + constant * exploration;
	}
	
}
