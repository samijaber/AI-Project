package odd;

import java.util.LinkedList;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class MyPlayer extends Player {
	OddBoard currBoard;
	private OddRandomPlayer rnd1 = new OddRandomPlayer();
	private int TIME_CUTOFF = 4000;
	private int turn = 0;
	private Node leaf;
	public static Node root;
	private int first = 0;
	private double UCBconstant = 2;
	private double minRollouts = 10;

	public MyPlayer() {
		super("The Sistah");
		currBoard = null;
	}
	
	public MyPlayer(String name) {
		super("The Sistah");
		currBoard = null;
	}
	
    public void movePlayed( Board board, Move move ) {
	System.out.println( "Move: " + move.toPrettyString() ); 
	
	//if (first != 0)
		//root = root.hasChild((OddMove) move);

	}
	

	@Override
	public Move chooseMove(Board board) {
		long start = System.currentTimeMillis();
		currBoard = (OddBoard) board;
		turn = board.getTurn();
		root = new Node();
		
		/*
		if(first == 0)
		{
			root = new Node();
			first ++;
		}
		else
		{
			root.parent = null;
			//System.out.println("THE CURRENT ROOT MOVE IS: " + root.move.toPrettyString());
		}
		*/
		
		
		LinkedList<OddMove> valmoves = currBoard.getValidMoves();
		
		Node next = new Node();
		
		for(int j = 0; j < minRollouts; j++)
		{
			for (int i = 0; i < valmoves.size(); i++) {
				OddBoard boardClone = (OddBoard) currBoard.clone();
				next = new Node(valmoves.get(i), root, UCBconstant);
				boardClone.move(next.move);	
				next = root.hasChild(next.move);
				//Step 3: Simulation
				int winner = simulation(next, boardClone);
				
				//Step 4: Back propagation
				backprop(leaf, winner);

			}
		}
		while((System.currentTimeMillis() - start) < TIME_CUTOFF) {

			monteCarlo(root);
		}
		//System.out.println(System.currentTimeMillis() - start);
		
		Node best = selectBestMove(root);
		return best.move;
	}

	public void monteCarlo(Node root) {
		OddBoard board = (OddBoard) currBoard.clone();		
		//Step 1/2: Selection + expansion
		Node selected = selection(root, board);
		//Step 3: Simulation
		int winner = simulation(selected, board);
		//Step 4: Back propagation
		backprop(leaf, winner);
	}
	
	public void backprop(Node n, int score){
		n.wins += score;
		n.visited ++;
		if (!n.isRoot())
			backprop(n.parent, score);
	}
	
	/*
	 * Deterministic selection and execution up until some node.
	 */
	public Node selection(Node root, OddBoard board){
		Node n = root;
		boolean leaf = false;
		boolean max = true;
		
		if(n.child.isEmpty())
			leaf = true;
		
		while (!leaf) {
			Node next = new Node();
			double unexplored = Math.sqrt(Math.log(n.visited));
			
			if(max) {
				double maxvisited = 0;
				for (Node c : n.child) {
					if (c.UCB() > maxvisited) {
						next = c;
						maxvisited = c.UCB();
					}
				}


				if ((unexplored >= maxvisited) && board.getValidMoves().size() != n.child.size()) {
					OddMove chosen = pickMove(board.getValidMoves(), n);
					next = new Node(chosen, n, UCBconstant);
				}
			}
			else {
				double minvisited = 100;
				for (Node c : n.child){
					if (c.UCB() < minvisited) {
						next = c;
						minvisited = c.UCB();
					}
				}
				
				if ((unexplored <= minvisited) && board.getValidMoves().size() != n.child.size()) {
					OddMove chosen = pickMove(board.getValidMoves(), n);
					next = new Node(chosen, n, UCBconstant);
				}
			}
			
			board.move(next.move);
			n = next;
			max = !max;
			
			if(n.child.isEmpty())
				leaf = true;
		}
		return n;
	}
	
	
	public OddMove pickMove(LinkedList<OddMove> valid, Node n){
		for (OddMove m : valid)
		{
			Node child = n.hasChild(m);
			if(child == null)
			{
				return m;
			}
		}
		return null;	
	}

	
	public int simulation(Node n, OddBoard board){
		Node prev = n;
		
		while(board.countEmptyPositions() > 0)
		{
			OddMove chosenmove = (OddMove) rnd1.chooseMove(board);
			Node childcheck = prev.hasChild(chosenmove);
			
			if (childcheck == null)
			{
				Node newmove = new Node(chosenmove, prev, UCBconstant);
				prev = newmove;
			}
			else
			{
				prev = childcheck;
			}
			
			board.move(chosenmove);
		}
		
		int winner = board.getWinner();
	
		if(winner == turn)
		{
			winner = 1;
		}
		else
		{
			winner = 0;
		}
		
		leaf = prev;
		return winner;
	}
	
	public Node selectBestMove(Node root){
		Node best = root.child.get(0);
		for(Node n : root.child)
		{
			if(n.getwinRate() > best.getwinRate())
			{
				best = n;
			}
		}
		return best;
	}
}

class Node {
	OddMove move;
	public double wins;
	public double visited;
	Node parent;
	LinkedList<Node> child;
	public double constant = 1;
	
	public Node() {
		wins = 0;
		visited = 0;
		move = null;
		parent = null;
		child = new LinkedList<Node>();
	}
	
	public Node(OddMove move, Node parent, double constant){
		this.move = move;
		this.parent = parent;
		wins = 0;
		visited = 0;
		this.constant = constant;
		child = new LinkedList<Node>();
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
	
	public Node removeChild(OddMove move) {

		for (Node n: child)
		{
			if ((n.move.destCol == move.destCol) && (n.move.destRow == move.destRow))
			{
				if(n.move.color == move.color)
					child.remove(n);
					return n;
			}
		}
		return null;
	}
	
	public double getwinRate(){
		return wins/visited;
	}
	
	public void addChild(Node ch) {
		if (hasChild(ch.move) == null)
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
		
		return getwinRate() + constant * exploration;
	}
	
}