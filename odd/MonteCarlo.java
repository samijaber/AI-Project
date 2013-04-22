package odd;

import java.util.LinkedList;

public class MonteCarlo implements Runnable {
	private OddBoard currBoard;	
	private Node leaf;
	private int turn = 0;
	private OddRandomPlayer rnd1 = new OddRandomPlayer();
	private Node root;

	public MonteCarlo(OddBoard currBoard, int turn, Node root){
		this.currBoard = currBoard;
		this.root = root;
		this.turn = turn;
	}

	@Override
	public void run() {
		//create a deep copy of the current board.
		OddBoard board = (OddBoard) currBoard.clone();
		
		//Step 1: Selection
		Node selected = selection(MyPlayer.root, board);
		
		//Step 2: Expansion
		expand(selected, board);
		
		//Step 3: Simulation
		int winner = simulation(selected, board);
		
		//Step 4: Back propagation
		backprop(leaf, winner);		
	}
	
	public Node getRoot(){
		return root;
	}
	
	public void monteCarlo(Node root) {
		//create a deep copy of the current board.
		OddBoard board = (OddBoard) currBoard.clone();
		
		//Step 1: Selection
		Node selected = selection(root, board);
		
		//Step 2: Expansion
		expand(selected, board);
		
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
					next = new Node(chosen, n);
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
					next = new Node(chosen, n);
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
	
	public void expand(Node n, OddBoard board){
		
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

}
