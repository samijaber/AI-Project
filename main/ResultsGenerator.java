package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ResultsGenerator {

	// If you've extracted the files correctly then you shouldn't
	// need to change any of the values below :)
	private static final String PROPERTIES_FILENAME = "test.properties",
						 		DEFAULT_OUTCOMES_PATH = "logs/outcomes.txt",
						 		DEFAULT_RESULTS_PATH = "logs/stats.txt";
	
	// Because I'm parsing the .properties file I can't declare these as final:
	private static String OUTCOMES_PATH, RESULTS_PATH, PLAYER_1, PLAYER_2;
	
	private static int scoreOfAIOne = 0, scoreOfAITwo = 0, playerOneScore = 0, playerTwoScore = 0;
	
	public static void main (String[] args){

		Properties prop = new Properties();

		// Load player names from properties file
		try {
			prop.load(new FileInputStream(PROPERTIES_FILENAME));
			PLAYER_1 = prop.getProperty("player1");
			PLAYER_2 = prop.getProperty("player2");
			OUTCOMES_PATH = prop.getProperty("outcomes_path");
			RESULTS_PATH = prop.getProperty("results_path");
		} catch (IOException e) {
			e.printStackTrace();
			PLAYER_1 = "Player1";
			PLAYER_2 = "Player2";
			OUTCOMES_PATH = DEFAULT_OUTCOMES_PATH;
			RESULTS_PATH = DEFAULT_RESULTS_PATH;
		}
		
		if (OUTCOMES_PATH == null) OUTCOMES_PATH = DEFAULT_OUTCOMES_PATH;
		if (RESULTS_PATH == null) RESULTS_PATH = DEFAULT_RESULTS_PATH;
		
		parseOutcomes();
		try {
			writeOutput();
		} catch (IOException e) {
			/** NOTE: I haven't tested this block.
			 * In case the file I/O messes up while you've started the
			 * batch script by double clicking on it, this should allow you
			 * to see the stats printed in the console before quitting.
			 */
			System.out.print("Press enter to continue . . . ");
			new Scanner(System.in).nextLine();
		}
	}
	
	private static void parseOutcomes(){
		
		// Load the outcomes file
		File f = new File(OUTCOMES_PATH);
		Scanner reader = null;
		try {
			reader = new Scanner(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Parse the file to gather the statistics
		StringTokenizer tokenizer = null;

		String line = null, winner = null;
		
		while (reader.hasNext()){

			line = reader.nextLine();
			if (line.equals("")) continue; // Blank line
			
			// This is a bit ugly =P
			tokenizer = new StringTokenizer(line);
			tokenizer.nextToken();
			String playerOne = tokenizer.nextToken(), playerTwo = tokenizer.nextToken();
			if (!isGameOfInterest(playerOne, playerTwo)) continue;

			winner = tokenizer.nextToken();
			// Record which player won (as in, Player 1/2, regardless of AI names)
			if (winner.equals("Player1")){
				playerOneScore++;
			}
			else {
				playerTwoScore++;
			}
			
			// Record the *name* of the winner
			winner = tokenizer.nextToken();
			if (winner.equals(PLAYER_1)){
				scoreOfAIOne++;
			}
			else if (winner.equals(PLAYER_2)){
				scoreOfAITwo++;
			}

		}

		reader.close();
	}
	
	private static boolean isGameOfInterest(String playerOne, String playerTwo){
		return (playerOne.equals(PLAYER_1) && playerTwo.equals(PLAYER_2)
			 || playerOne.equals(PLAYER_2) && playerTwo.equals(PLAYER_1));
	}
	
	private static void writeOutput() throws IOException {
		
		FileWriter writer = null;
		File fout = new File(new File(RESULTS_PATH).getAbsolutePath());
		if (!fout.exists()){
			try {
				fout.createNewFile();
			} catch (IOException e) {
				// I don't know why this would ever happen
				// if you've done all the steps correctly ^^
				e.printStackTrace();
			}
		}
		
		try {
			writer = new FileWriter(fout, true);
			// Write to the output file
			writer.write(PLAYER_1 + " vs. " + PLAYER_2 + ": " + scoreOfAIOne + " - " + scoreOfAITwo + "\r\n");
			writer.write("Player1 vs. Player2: " + playerOneScore + " - " + playerTwoScore + "\r\n");
			writer.write("\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			// Display to console instead
			System.out.println("----- RESULTS ------");
			System.out.println(PLAYER_1 + " vs. " + PLAYER_2 + ": " + scoreOfAIOne + " - " + scoreOfAITwo);
			System.out.println("Player1 vs. Player2: " + playerOneScore + " - " + playerTwoScore);
			System.out.println("--------------------");
			throw e;
		}
		
	}
}
