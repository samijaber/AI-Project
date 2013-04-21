1. Extract the contents in your src folder (in case your don't
   have the typical Eclipse -not sure about NetBeans- project structure, 
   just paste it in the same folder as your other packages
   (boardgame, odd, etc.)). The "logs" folder should be within
   that directory too. If it isn't, move it there.

2. Inside AutomatedTester.java change the constructor calls to
   match the AI classes you want to test (lines 53, 54).

3. test.properties contains all the parameters you might want to set - 
   make sure to read the comments in this file.
   IMPORTANT: Ensure that the names you give to the players correspond
   	      to the arguments you pass to the parent class' (Player)
	      constructor for each of the AIs you're testing. For example, if 
	      your AI class is named MyAIPlayer but in its constructor MyAIPlayer() 
	      you do super("ub3rpwn4g3"); and your opponent is OddRandomPlayer 
	      (in the constructor of which there is a super("OddRandomPlayer"); 
	      call) then you should set player1=ub3rpwn4g3 and player2=OddRandomPlayer
	      (or the other way around, it doesn't matter since they get shuffled) to
	      get the correct results in stats.txt.

4. Run test.bat to play the amount of games specified in test.properties.
   I have not made a bash script since I'm on Windows.

5. Your results should be in logs/stats.txt. Between tests that you run,
   statistics for your latest run will be being appended to the end of this file
   (basically the statistics summarize the contents of outcomes.txt -
   which AI player won more times and how many times the "odd" player won).