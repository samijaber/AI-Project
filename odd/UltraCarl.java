package odd;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

/**
 * @author Tina Latif
 */
public class UltraCarl extends Player {
	OddMove lastMove;
	AtomicBoolean moveAvailable;
	AtomicBoolean moveRequested;
	OddBoard localBoard;
	OddMove nextMove;
	Thread mcThread; 
	int myTurn;
	private boolean lastTurn;
	
	Random R = new Random();
	
    public UltraCarl() {
        super("UltraCarl");
        moveAvailable = new AtomicBoolean(false);
        moveRequested = new AtomicBoolean(false);
        lastTurn = false;
        
        localBoard = new OddBoard();
        
        mcThread = new Thread(new MonteCarloSearchTree());
        mcThread.start();      
    }
    
    
    public class MonteCarloSearchTree implements Runnable {
    	MCNode root;
    	
    	public MonteCarloSearchTree() {
    		root = new MCNode(null, null);
    	}
    	public void run() {
    		/* catch and ignore the first boardUpdate flag
    		 * this only matters on subsequent moves
    		 * when you need to find the opponents move in the tree */
    		while(moveAvailable.get() == false) { /* wait */ }
    		if(lastMove != null)  {
    			localBoard.move(lastMove);
    			root = new MCNode(lastMove, null);
    			myTurn = 2;
    		}
    		else{
    			myTurn = 1;
    		}
    		moveAvailable.set(false);
    		
    		while(true) {
    			/* if there is a new updated move, search the root's children,
    			 * find the opponent's move, and make that the new root of the MCST */
    			if(moveAvailable.get() == true) {
    				adjustRoot();
    				localBoard.move(lastMove);
    				moveAvailable.set(false);
    			}
    			
    			/* if a move has been requested, determine the best child of the root
    			 * save it as the nextMove, update the moveRequest flag, and 
    			 * update the root of the MCST */
    			else if(moveRequested.get() == true) {
    				MCNode bestChild = findBestMove();
    				nextMove = bestChild.move;
    				localBoard.move(nextMove);
    				root = bestChild;
    				bestChild.parent = null;
    				moveRequested.set(false);
    				if(lastTurn)
    					break;
    			}
    			
    			else {
    				// work on tree
    				// traverse down until you hit a leaf
        			MCNode current = root;
        			
        			/* make a copy of the local board for the purpose of traversing
        			 * Note that the local board already has the root played on it */
        			OddBoard traversalBoard = new OddBoard(localBoard);
        			while(current.children.size() != 0) {
        				if(current.children.getFirst().move.getPlayerID() == myTurn) {
        					// my move to be simulated (i.e. use UCB beneficial for me)
        					current = getMyBestChild(current);
        					traversalBoard.move(current.move);
        				}
        				else {
        					// opponents move to be simulated (i.e. use UCB beneficial for them)
        					current = getOpponentsBestChild(current);
        					traversalBoard.move(current.move);
        				}
        			}
        			// expand leaf  			
        			expandNode(traversalBoard, current);
        		
        			// rollout
        			rollout(traversalBoard, current);
    			}
    			
  			
    		}   		
    	}
    	
    	/**
    	 * Given the best "leaf" (it has been expanded but all children are empty/unvisited), 
    	 * choose a random child, then do 100 rollouts from that point on (incl. random child's move)
    	 * The statistics accumulated on the 100 rollouts should be backwards-applied to ancestors.
    	 * @param traversalBoard the board at the time of rollout (includes rolloutNode's move)
    	 * @param rolloutNode best leaf to rollout on a random child of
    	 */
    	private void rollout(OddBoard traversalBoard, MCNode rolloutNode) {
    		int rollouts = 5;
    		int wins = 0;
    		
    		if(traversalBoard.getValidMoves().size() == 0) {
    			wins = (traversalBoard.getWinner() == myTurn) ? rollouts : 0;
    		}
    		else {
	    		// choose a random child. It's move has NOT been played.
	    		Random R = new Random();
	    		MCNode randomChild = rolloutNode.children.get(R.nextInt(rolloutNode.children.size()));
	 		
	    		for(int i = 0; i < rollouts; i++) {
	    			/* Play a simulated game. 
	    			 * Make a copy of the traversalBoard. Make the random child's move on the board.
	    			 * Then, until the game is over, play random valid moves, and update the wins. */
	    			OddBoard rolloutBoard = new OddBoard(traversalBoard);
	    			rolloutBoard.move(randomChild.move);
	    			while(rolloutBoard.getValidMoves().size() != 0) {
	    				LinkedList<OddMove> validMoves = rolloutBoard.getValidMoves();
	    				rolloutBoard.move(validMoves.get(R.nextInt(validMoves.size())));
	    			}
	    			wins += (rolloutBoard.getWinner() == myTurn) ? 1 : 0;
	    		}
	    		// apply statistics to randomChild
	    		randomChild.wins += wins;
	    		randomChild.visits++;
    		}
    		
    		// apply statistics to all ancestors (excl. randomly chosen child - already applied stats)
    		MCNode current = rolloutNode;
    		do {
    			current.wins += wins;
    			current.visits++;
    			current = current.parent;
    		} while(current != null);
    	    		
    	}

    	private void expandNode(OddBoard traversalBoard, MCNode nodeToExpand) {
    		for(OddMove move : traversalBoard.getValidMoves()) {
    			MCNode newNode = new MCNode(move, nodeToExpand);
    			nodeToExpand.children.add(newNode);
    		}
    	}
    	
    	private MCNode getMyBestChild(MCNode node) {
    		MCNode bestChild = node.children.getFirst();
    		double bestChildStat = node.children.getFirst().myUCB();
    		for(MCNode child : node.children) {
    			if(child.myUCB() > bestChildStat) {
    				bestChildStat = child.myUCB();
    				bestChild = child;
    			}
    		}
    		return bestChild;
    	}
    	
    	private MCNode getOpponentsBestChild(MCNode node) {
    		MCNode bestChild = node.children.getFirst();
    		double bestChildStat = node.children.getFirst().opponentsUCB();
    		for(MCNode child : node.children) {
    			if(child.opponentsUCB() > bestChildStat) {
    				bestChildStat = child.opponentsUCB();
    				bestChild = child;
    			}
    		}
    		return bestChild;
    	}
    	
    	private MCNode findBestMove() {
    		MCNode bestMove = root.children.getFirst();
    		double bestMoveStat = root.children.getFirst().winRate();
    		for(MCNode move : root.children) {
    			if(move.winRate() > bestMoveStat) {
    				bestMoveStat = move.winRate();
    				bestMove = move;
    			}
    		}
    		return bestMove;
    	}
    	
    	private void adjustRoot() {
    		for(MCNode child : root.children) {
    			if(lastMove.equals(child.move)) {
    				root = child;
    				child.parent = null;
    				break;
    			}
    		}
    	}
    	
    	
    }
     
    public class MCNode {
    	// board state information
    	OddMove move;
    
    	// tree data
    	LinkedList<MCNode> children;
    	MCNode parent;
    	
    	// stats
    	double wins;
    	double visits;
    	
    	public MCNode(OddMove pMove, MCNode pParent) {
    		move = pMove;
    		
    		parent = pParent;
    		children = new LinkedList<MCNode>();
    		
    		wins = 0;
    		visits = 0;
    	}
    	
    	public double myUCB() {
    		if(visits == 0) {
    			return 3*Math.sqrt(Math.log(parent.visits));
    		}
    		return wins/visits + 3*Math.sqrt(Math.log(parent.visits)/visits);
       	}
    	
    	public double opponentsUCB() {
    		if(visits == 0) {
    			return 3*Math.sqrt(Math.log(parent.visits));
    		}
    		return 1 - wins/visits + 3*Math.sqrt(Math.log(parent.visits)/visits);
    	} 
    	
    	public double winRate() {
    		if(visits == 0)
    			return 0;
    		return wins/visits;
    	}
    }

    public Move chooseMove(Board board) {
    	// set boardAvailable flag so MCST can update its root
    	if(((OddBoard)board).getValidMoves().size() <= 4) 
    		lastTurn = true;
    	lastMove = ((OddBoard)board).getLastMove();
        moveAvailable.set(true);
        
        // wait while MCST expands tree
        long t0, t1;
        t0 = System.currentTimeMillis();
        do{
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < 4000);
        
        // set request flag and wait for a move
        moveRequested.set(true);
        while(moveRequested.get() == true) { /* wait for move */ }
        return nextMove;       
    }

    /**
     * Obligatory overwrite even though Board.createBoard() returns an OddBoard
     */
    public Board createBoard() {
        return new OddBoard();
    }
}