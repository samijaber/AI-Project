package boardgame;

public class helper {
	Move goodMove;
	int wins=0;
	public helper(Move goodMove,int wins){
		this.goodMove=goodMove;
		this.wins=wins;
	}
	public int getInt(){
		return this.wins;
	}
	public Move getMove(){
		return this.goodMove;
	}
	

}
