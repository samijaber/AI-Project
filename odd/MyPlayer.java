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

	public MyPlayer() {
		super("THE MIGHTY AI");
		currBoard = null;
	}
	
	public MyPlayer(String name) {
		super("THE MIGHTY AI");
		currBoard = null;
	}
	

	@Override
	public Move chooseMove(Board board) {
		long start = System.currentTimeMillis();
		currBoard = (OddBoard) board;
		turn = board.getTurn();
		
		Node root = new Node();

		while((System.currentTimeMillis() - start) < TIME_CUTOFF) {
			monteCarlo(root);
		}
		
		Move best = selectBestMove(root);
		
		return best;
	}
	
	public void monteCarlo(Node root) {
		//create a deep copy of the current board.
		OddBoard board = (OddBoard) currBoard.clone();
		
		//Step 1: Selection
		Node selected = selection(root, board);
		
		//Step 2: Expansion
		
		//Step 3: Simulation
		int winner = simulation(selected, board);
		
		//Step 4: Back propagation
		backprop(leaf, winner);
	}
	
	public void backprop(Node n, int score){
		//Make sure score is determined properly in Simulation.
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
		
		while (!leaf)
		{
			Node next = new Node();
			
			//get value of general unexplored node
			double unexplored = Math.sqrt(Math.log(n.visited));
			double maxvisited = 0;
			for (Node c : n.child)
			{
				if (c.UCB() > maxvisited)
				{
					next = c;
					maxvisited = c.UCB();
				}
			}
			
			
			if ((unexplored >= maxvisited) && board.getValidMoves().size() != n.child.size())
			{
				//all visited have lower value than an unexplored one
				//pick an unexplored one at random from possible moves
				if (!n.isRoot())
				{
					board.move(n.move);
				}
				OddMove chosen = pickMove(board.getValidMoves(), n);
				n = new Node(chosen, n);
			}
			else
			{
				if(!n.isRoot())
					board.move(n.move);
				
				n = next;
				
			}
			
			if(n.child.isEmpty())
			{
				leaf = true;
			}
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
				Node newmove = new Node(chosenmove, prev);
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
	
	public Move selectBestMove(Node root){
		Node best = root.child.get(0);
		for(Node n : root.child)
		{
			if(n.getwinRate() > best.getwinRate())
			{
				best = n;
			}
		}
		return best.move;
	}
}