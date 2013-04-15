package odd;

import java.util.LinkedList;

import odd.OddBoard.Piece;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class OddAIPlayer extends Player {
	OddBoard currBoard;
	private LinkedList<OddMove> valid_moves;
	private LinkedList<OddMove> score_moves;

	public OddAIPlayer(String name) {
		super(name);
		currBoard = null;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Move chooseMove(Board board) {
		currBoard = (OddBoard) board;
		valid_moves = currBoard.getValidMoves();
		int clusters = getNumClusters(currBoard);
		return null;
	}
	
	public void backprop(Node n, int score){
		
	}
	
	public void selection(){
		
	}
	
	public void simulation(){
		
	}
	
    //Same as DetermineWinner() basically. Returns the current number of clusters
    public int getNumClusters(OddBoard board) {
    	int SIZE= OddBoard.SIZE;
    	int SIZE_DATA = SIZE*2+1;
    	
    	int[] parent = new int[SIZE_DATA * SIZE_DATA];
    	for (int k = 0; k < parent.length; k++) parent[k] = -1;
    	
    	int crt_index = 0;
    	for (int j = -SIZE; j <= SIZE; j++)
			for (int i = -SIZE; i <= SIZE; i++, crt_index++) {
				Piece crt_piece = board.getPieceAt(i, j);
				if(crt_piece == Piece.INVALID || crt_piece == Piece.EMPTY) continue;
				
				int connection_code = 0; // used to know which neighbours are connected		
				if(board.getPieceAt(i - 1, j - 1) == crt_piece) connection_code++;
				if(board.getPieceAt(i, j - 1) == crt_piece) connection_code += 2;
				if(board.getPieceAt(i - 1, j) == crt_piece) connection_code += 4;
				
				switch (connection_code) {
				case 1: 
				case 2:
				case 3: // one connection, have to look for root
					//find root
					int y = crt_index - SIZE_DATA - ((connection_code == 1) ? 1 : 0);
					do{
						int tmp = y;
						y = parent[y];
						parent[tmp] = crt_index; 
					}while (y >= 0);
					parent[crt_index] += y;
					break;
				case 6: // two connections
					//find root
					y = crt_index - SIZE_DATA; int z=y;
					do{
						z = y;
						y = parent[y];
						parent[z] = crt_index; 
					}while (y >= 0);					
					// if root not the same, then we have to connect the second piece
					parent[crt_index] += y;
					if( z == crt_index - 1) break;
				default:  //one connection to its closest neighbour
					parent[crt_index] += parent[crt_index-1];
					parent[crt_index-1] = crt_index;
					break;					
				case 0: // no neighbours
					break;
				}
			}
    	int num_clusters = 0;
		for (int k = 0; k < parent.length; k++) {
			if(parent[k] <= -OddBoard.MIN_CLUSTER_SIZE) num_clusters++;
			int y = k;
			while (y >= 0) y = parent[y];
			if(y <= -OddBoard.MIN_CLUSTER_SIZE) 
				board.getBoardData()[k % SIZE_DATA][k / SIZE_DATA] = 
					( board.getBoardData()[k % SIZE_DATA][k / SIZE_DATA] == Piece.WP ) ?
							Piece.WP_CLUST : Piece.BP_CLUST;
		}
    	return num_clusters;
    }

}