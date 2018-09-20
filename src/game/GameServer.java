package game;

import java.awt.CardLayout;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.sound.midi.MidiDevice.Info;

import com.google.gson.Gson;

import model.Brawler;
import model.Brawlers;
import model.InfoObj;

public class GameServer {
	
	private int roomNum;
	private ServerSocket serverSocket;
	private ArrayList<Brawler> brawlerList;
	
	private ArrayList<ServerThread> serverThreads;
	
	private Map<String, Boolean> fullMap;
	
	public GameServer() {
		serverThreads = new ArrayList<ServerThread>();
		roomNum = 0;
		
		fullMap = new HashMap<>();
	
	}
	
	public void startServer() {
		// get a port
		while(true) {
			try {
				System.out.println("please enter a valid port: ");
				Scanner input = new Scanner(System.in);
				int port = input.nextInt();
				serverSocket = new ServerSocket(port);
				
				break;	
			} catch (Exception e) {
				System.out.println("Invalid port!");
			}
		}

		// read from JSON file
		FileReader fReader;
		Gson gson = new Gson();
		
		while(true) {
			System.out.println("Please enter a valid file:");
			Scanner scan = new Scanner(System.in);
			String fileName = scan.nextLine();
			try {
				fReader = new FileReader(fileName);
				Brawlers brawlers = gson.fromJson(fReader, Brawlers.class);
				brawlerList = brawlers.getBrawlers();
				
				break;
			} catch (FileNotFoundException e) {
				System.out.println("Invalid file! ");
			}
		}
		
		System.out.println("Game Server Starts Successfully!");
		
		getNewPlayer();
	}

	
//	public void startServer() {
//		try {
//			this.serverSocket = new ServerSocket(6789);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		// read from JSON file
//		FileReader fReader;
//		Gson gson = new Gson();
//		
//		try {
//			fReader = new FileReader("sample.json");
//			Brawlers brawlers = gson.fromJson(fReader, Brawlers.class);
//			brawlerList = brawlers.getBrawlers();
//			
//		} catch (FileNotFoundException e) {
//			System.out.println("Invalid file! ");
//		} catch (Exception e) {
//			// TODO: handle exception
//			System.out.println("exception in GameServer startServer()");
//			e.printStackTrace();
//		}
//		
//		System.out.println("Game Server Starts Successfully!");
//		
//		getNewPlayer();
//	}
	
	public void getNewPlayer() {
		try {
			while(true) {
				System.out.println("waiting for connection...");
				Socket socket = serverSocket.accept();
				System.out.println("connection from " + socket.getInetAddress());
				
				ServerThread serverThread = new ServerThread(socket, this, "");
				serverThreads.add(serverThread);

				
			}
		} catch (IOException e) {
			System.out.println("ioe int GameServer constructor: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("exception-------------------------------");
			e.printStackTrace();
		}
	}
	
	public void removeServerThread(ServerThread serverThread) {
		serverThreads.remove(serverThread);
	}
	
	// send info to both players
	public void broadcast(InfoObj info, String roomName) {
		System.out.println("-------------------Broadcasting--------------------");
		System.out.println(info.getMessage());
		for(ServerThread st : serverThreads) {
			if(st.getRoomName().equals(roomName)) {
				st.sendMessage(info);
			}
		}
	}
	
	// get your opponent thread
	public ServerThread getOpponent(String roomName, ServerThread serverThread) {
		for(ServerThread sThread : serverThreads) {
			if(sThread.getRoomName().equals(roomName) && sThread != serverThread) {
				return sThread;
			}
		}
		return null;
	}
	
	// send info to oponent player
	public void sendToOpponent (InfoObj info, String roomName, ServerThread serverThread) {
		for(ServerThread sThread : serverThreads) {
			if(sThread.getRoomName().equals(roomName) && sThread != serverThread) {
				sThread.sendMessage(info);
			}
		}
	}
	
	public void serverLog(String line) {
		System.out.println("Server Log: " + line);
	}
	
	public ArrayList<ServerThread> getServerThreads() {
		return serverThreads;
	}

	public void setServerThreads(ArrayList<ServerThread> serverThreads) {
		this.serverThreads = serverThreads;
	}

	public Map<String, Boolean> getFullMap() {
		return fullMap;
	}

	public void setFullMap(Map<String, Boolean> fullMap) {
		this.fullMap = fullMap;
	}

	public ArrayList<Brawler> getBrawlerList() {
		return brawlerList;
	}

	public void setBrawlerList(ArrayList<Brawler> brawlerList) {
		this.brawlerList = brawlerList;
	}


	
	
	public static void main(String[] args) {
		GameServer gameServer = new GameServer();
		gameServer.startServer();
	}
}
