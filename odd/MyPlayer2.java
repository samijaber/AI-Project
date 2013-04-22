package odd;

import java.util.LinkedList;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class MyPlayer2 extends Player {
	OddBoard currBoard;
	private OddRandomPlayer rnd1 = new OddRandomPlayer();
	private int TIME_CUTOFF = 3000;
	private int turn = 0;
	private Node leaf;
	public static Node root;
	private int first = 0;
	private double constant = 10;
	
	public MyPlayer2() {
		super("THE MIGHTY AI");
		currBoard = null;
	}
	
	public MyPlayer2(String name) {
		super("THE MIGHTY AI");
		currBoard = null;
	}
	
    public void movePlayed( Board board, Move move ) {
	System.out.println( "Move: " + move.toPrettyString() ); 
	
	if (first != 0)
		//Move root to played node.
		root = root.hasChild((OddMove) move);

	}
	

	@Override
	public Move chooseMove(Board board) {
		long start = System.currentTimeMillis();
		currBoard = (OddBoard) board;
		turn = board.getTurn();
		
		if(first == 0)
		{
			root = new Node();
			first ++;
		}
		else
		{
			//Node opponentMove = FindOppMove(currBoard.getValidMoves());
			//root = opponentMove;
			root.parent = null;
			System.out.println("THE CURRENT ROOT MOVE IS: " + root.move.toPrettyString());
		}
		
		while((System.currentTimeMillis() - start) < TIME_CUTOFF) {
			/*
			MonteCarlo mcts1 = new MonteCarlo(currBoard, turn, root);
			MonteCarlo mcts2 = new MonteCarlo(currBoard, turn, root);
			
			Thread t1 = new Thread(mcts1);
			Thread t2 = new Thread(mcts2);
			
			t1.start();
			t2.start();
		
			*/
			monteCarlo(root);
		}
		System.out.println(System.currentTimeMillis() - start);
		
		Node best = selectBestMove(root);
		//root = best; 
		return best.move;
	}

	public void monteCarlo(Node root) {
		//create a deep copy of the current board.
		OddBoard board = (OddBoard) currBoard.clone();
		
		//Step 1/2: Selection + expansion
		Node selected = selection(root, board);
		
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
		boolean max = true;
		
		if(n.child.isEmpty())
			leaf = true;
		
		while (!leaf)
		{
			Node next = new Node();
			
			//get value of general unexplored node
			double unexplored = Math.sqrt(Math.log(n.visited));
			
			if(max)
			{

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

					OddMove chosen = pickMove(board.getValidMoves(), n);
					next = new Node(chosen, n, constant);
				}
			}
			else
			{
				double minvisited = 100;
				for (Node c : n.child)
				{
					if (c.UCB() < minvisited)
					{
						next = c;
						minvisited = c.UCB();
					}
				}


				if ((unexplored <= minvisited) && board.getValidMoves().size() != n.child.size())
				{
					//all visited have lower value than an unexplored one
					//pick an unexplored one at random from possible moves
					OddMove chosen = pickMove(board.getValidMoves(), n);
					next = new Node(chosen, n, constant);
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
				Node newmove = new Node(chosenmove, prev, constant);
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
