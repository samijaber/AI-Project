	package odd;

	import boardgame.Board;
import boardgame.Move;
import boardgame.Player;
import java.util.LinkedList;
import java.util.Random;
import java.util.Date;
	
public class Monty extends Player{

	    public Monty() {
	        super("Monty");
	    }

	    public Move chooseMove(Board board) {
	        OddBoard pb = (OddBoard) board;
	        
	        MCTNode root = new MCTNode(pb, pb.getTurn());
	        long time = System.currentTimeMillis ();
	        
	        while (System.currentTimeMillis () - time < 3000) {
	        	root.selectAction();
	        }
	        return root.bestMove();
	        
	        
	        
	    }

	    public Board createBoard() {
	        return new OddBoard();
	    }
}

class MCTNode {
	static Random R = new Random();
//	public int x, y, 
	public int player; 	//position
//	public Piece color;
	OddMove parentMove;
	OddBoard cb;
	LinkedList<MCTNode> children = null;
	double totValue=0, nVisits=0;
	double epsilon = 1e-6;
	double bias = 10.0;
	
	public MCTNode(OddMove move, OddBoard board) {
	//	System.out.println(move.toPrettyString());
		
		String[] tokens = move.toTransportable().split(" ");
		player = Integer.parseInt(tokens[0]);
//		color = tokens[1].equals("WHITE") ? Piece.WP : Piece.BP;
//		x = Integer.parseInt(tokens[2]);
//		y = Integer.parseInt(tokens[3]);
		parentMove = move;
		
		cb = (OddBoard) board.clone();
		cb.move(move);
	}
	
	public MCTNode (OddBoard board, int player) {
		cb = (OddBoard) board.clone();
		this.player = player;
		
	}
	
	public void selectAction() {
        LinkedList<MCTNode> visited = new LinkedList<MCTNode>();
        MCTNode cur = this;
        visited.add(this);
 //       System.out.println(cur.totValue);
        double value=0;
        
        while (!cur.isLeaf()) {
        	MCTNode prev = cur;
            cur = cur.select(player);
            if(cur == null) {								//Handling terminal nodes.
            	//ugly hack
            	value = prev.cb.getWinner()== player ? 1 : 0;
            	break;
            }
            visited.add(cur);
        }
        
		if(cur!=null) {									//if it's not a terminal node.
			cur.expand();
			MCTNode newNode = cur.select(player);
			if (newNode != null) {						//Check again for newNode. Ew.
				visited.add(newNode);
				value = rollOut(newNode, player);
			}
			else value = cur.cb.getWinner()== player ? 1 : 0;
		}
		for (MCTNode node : visited) {
		node.updateStats(value);
		}
	}
	
	 public void expand() {
	        children = new LinkedList<MCTNode>();
	        for (OddMove m : cb.getValidMoves()) 
	            children.add(new MCTNode(m, cb));
//	        if(children.size()==0) children = null;
	        
	}
	 
	 public double rollOut(MCTNode node, int player) {
		 OddBoard theBoard = (OddBoard) cb.clone();
		 while(theBoard.getValidMoves().size()>0) {
			 LinkedList<OddMove> validMoves = theBoard.getValidMoves();
//			 System.out.println(theBoard.getTurnsPlayed());
//			 System.out.println(validMoves.size());
			 theBoard.move(validMoves.get(R.nextInt(validMoves.size()))); 
		 }
		 return theBoard.getWinner() == player? 1 : 0;
	 }
	
	
	
	
	//returns child with best value
	public MCTNode select(int player) {
		if(this.children.size()==0) return null;		//in case it's a terminal node.
		MCTNode selected = this.children.getFirst();
		double bestValue = Double.MIN_VALUE;

		if (this.player == player) {
			for (MCTNode c : this.children) {
				double uctValue = c.totValue/(c.nVisits + epsilon)
						+ bias* Math.sqrt(Math.log(nVisits + 1)/(c.nVisits + epsilon))
						+ R.nextDouble()* epsilon;
				// small random number to break ties randomly in unexpanded
				// nodes
				if (uctValue > bestValue) {
					selected = c;
					bestValue = uctValue;
				}
			}
		}
		
		else {
			for (MCTNode c : this.children) {
				double uctValue = (1-c.totValue/(c.nVisits + epsilon)) 				//this is the min player
						+ bias* Math.sqrt(Math.log(nVisits + 1)/(c.nVisits + epsilon))
						+ R.nextDouble()* epsilon;
				// small random number to break ties randomly in unexpanded
				// nodes
				
				if (uctValue > bestValue) {
					selected = c;
					bestValue = uctValue;
				}
			}
			

			
		}
		
		return selected;
	}
	
	public OddMove bestMove() {
		double bestValue = 0;
		OddMove bestAction = cb.getValidMoves().getFirst(); //initialization
		for (MCTNode c : children) {
			double value = c.totValue/c.nVisits;
			if (value >= bestValue) {
				bestValue = value;
				bestAction = c.parentMove;
			}
		}
		
		return bestAction;
	}
	
	 public void updateStats(double value) {
	        nVisits++;
	        totValue += value;
	 }
	
	public boolean isLeaf() {
	        return children == null;
	}
}