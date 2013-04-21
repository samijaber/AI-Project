package main;

import java.io.FileInputStream;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import boardgame.*;
import odd.*;

public class AutomatedTester {

	public static void main (String[] args){

		final String PROPERTIES_FILE = "test.properties",
					 DEFAULT_HOST = "localhost";
		final int DEFAULT_GAMES = 10,
				  DEFAULT_PORT = 8123,
				  TIMEOUT = 5000;

		Properties prop = new Properties();
		int numberOfGames = 0;

		try {
			prop.load(new FileInputStream(PROPERTIES_FILE));
			numberOfGames = Integer.parseInt(prop.getProperty("games"));
		} catch (IOException e){
			e.printStackTrace();
			numberOfGames = DEFAULT_GAMES;
		} catch (NumberFormatException e) {
			numberOfGames = DEFAULT_GAMES;
		}			 

		for (int i = 0; i < numberOfGames; i++){

			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(DEFAULT_PORT);
				// The following line should ensure that if you
				// terminate the tests abruptly, the socket can be
				// reused immediately if you re-launch
				serverSocket.setReuseAddress(true);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}

			Thread server = new Thread(new Server(new OddBoard(), false, true, serverSocket, TIMEOUT));
			ArrayList<Thread> clients = new ArrayList<Thread>(2);
			server.start();

			// TODO: Change the constructs below
			clients.add(new Thread(new Client(new MyPlayer(), DEFAULT_HOST, DEFAULT_PORT)));
			clients.add(new Thread(new Client(new OddRandomPlayer(), DEFAULT_HOST, DEFAULT_PORT)));
			Collections.shuffle(clients);	// Randomize who's player 1/2
			clients.get(0).start();
			try {
				Thread.sleep(500);
				clients.get(1).start();
				server.join();
				clients.get(0).join();
				clients.get(1).join();
				serverSocket.close();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}

		}
	}
}
