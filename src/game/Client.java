package game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import javax.sound.midi.MidiDevice.Info;

import model.ActiveBrawler;
import model.Brawler;
import model.InfoObj;

public class Client extends Thread{

 private String hostName;
 private int port;
 
 private Socket socket;
 
 private ObjectInputStream ois;
 private ObjectOutputStream oos;
 
 public Client(){
//	while(true) {
//	 	try {
//			System.out.println("Please enter an IP address: ");
//			Scanner input = new Scanner(System.in);
//			hostName = input.nextLine();
//			System.out.println("Please enter a port: ");
//			port = input.nextInt();
////			socket = new Socket(hostName, port);
//			
//			break;
//			
//		} catch (Exception e) {
//			System.out.println("Unable to connect!");
//		}
// 	}
//	 
//	try {
//		this.socket = new Socket(hostName, port);
//		System.out.println("Sucess!");
//		ois = new ObjectInputStream(socket.getInputStream());
//		oos = new ObjectOutputStream(socket.getOutputStream());
//		
//	} catch (IOException e) {
//		System.out.println("ioe in Clinet constructor");
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
	 
	 

		while(true) {
		 	try {
				System.out.println("Please enter an IP address: ");
				Scanner input = new Scanner(System.in);
				hostName = input.nextLine();
				System.out.println("Please enter a port: ");
				port = input.nextInt();
//				socket = new Socket(hostName, port);
				

				this.socket = new Socket(hostName, port);
				System.out.println("Sucess!");
				ois = new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());
				
				break;
				
			} catch (IOException e) {
				System.out.println("Unable to connect!");
			} catch (Exception e) {
				System.out.println("Unable to connect!");
			}
	 	}
	 
	 
	
	this.start();
 }
 	
 	
 	public void run() {
		while(true) {			
	 		try { 
	 			InfoObj info = (InfoObj)ois.readObject();
 				String infoType = info.getInfoType();
				if(infoType.equals("start")) {
					System.out.println("--------get start signal from server-------");
					iniGame(info.getRoomMap());
				}
				else if(infoType.equals("begin")) {
					System.out.println(info.getMessage());
//					System.out.println("+++++both preared+++++");
					
					InfoObj beginSignal = new InfoObj();
					beginSignal.setInfoType("prepared");    // the user is ready to choose brawlers
					oos.writeObject(beginSignal);
					oos.flush();
				}
				else if(infoType.equals("pick")) {			// let the user pick brawlers
//					System.out.println("now begin to pick");
					pickBrawler(info);
				}
				else if(infoType.equals("move")) {
					String prompt_str = info.getMessage();
					System.out.println(prompt_str);
					int move = moveInput(info);
					
					InfoObj moveInfo = new InfoObj();
					moveInfo.setInfoType("moved");
					moveInfo.setMove(move);
					oos.writeObject(moveInfo);
					oos.flush();
				}
				else if(infoType.equals("calculated")) {	// receive the attack information
					System.out.println(info.getMessage());
				}
				else if (infoType.equals("results")) {
					System.out.println(info.getMessage());
				}
				else if(infoType.equals("gameover")){
					System.out.println(info.getMessage());
					break;
				}
				
				
				
				else if(infoType.equals("single")) {
					System.out.println(info.getMessage());
					SinglePlay singlePlay = new SinglePlay(info.getBrawlerList());
				}
				else if(infoType.equals("message")) {
					System.out.println(info.getMessage());
				}
			} catch (ClassNotFoundException e) {
					System.out.println("cnfe in Client run()");
					break;
			}catch (IOException e) {
				System.out.println("ioe in Client run()");
				e.printStackTrace();
				break;
			} catch (Exception e) {
				System.out.println("Fail to connect server!");
//				e.printStackTrace();
				break;
			} 
		}
	}
 	public void iniGame(Map<String, Boolean> roomMap) {
 		System.out.println("Please make a choice: ");
 		System.out.println("1) Start Game ");
 		System.out.println("2) Join Game ");
 		String gameChoice_str = "";
 		Scanner input = new Scanner(System.in);
 		String choice = input.next();
 		gameChoice_str += choice;
 		String gameName = "";                                          
 		
 		// choose to start or join a game 
 		try {
 			// start a new game
	 		if(choice.equals("1")) {
	 	 		InfoObj info = new InfoObj();  
	 	 		info.setInfoType("new");
	 	 		String roomName = "";
	 			while(true) {
		 			System.out.println("What will you name your game?");
	 				roomName = input.next();

	 				if(roomMap.get(roomName)!=null) {
	 					System.out.println("This game already exists!");
	 					continue;
	 				}
	 				
	 				System.out.println("Valid name!");
			 		info.setRoomName(roomName);
	 				break;
	 			}	 
	 			// decide the number of players
	 			System.out.println("How many users?\n1 or 2");
	 			int userNum = input.nextInt();
	 			info.setPlayerNum(userNum);
	 			
	 			oos.writeObject(info);
	 			oos.flush();
	 		}
	 		// join an existent game
	 		else {		
	 	 		InfoObj info = new InfoObj();  
	 			info.setInfoType("join");
	 			while(true){
		 			System.out.println("Which game do you want to join?");
		 			String roomName = input.next();
		 			
		 			if(roomMap.get(roomName)!=null) {
		 				if(!roomMap.get(roomName)) {
		 					System.out.println("The room is not full, join it!");
		 					info.setRoomName(roomName);
		 					info.setPlayerNum(2);
				 			oos.writeObject(info);
				 			oos.flush();
		 					break;
		 				}
		 			}
		 			System.out.println("The room is full or does not exsit!");
	 			}
	 		}
 		} catch (IOException e) {
			System.out.println("ioe in startGame()");
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}
 	
 	public void pickBrawler(InfoObj info) {
 		Scanner input = new Scanner(System.in);
 		ArrayList<Brawler> brawlerList = info.getBrawlerList();
		ArrayList<ActiveBrawler> activeList = new ArrayList<ActiveBrawler>();
		while(true) {
			System.out.println(info.getMessage());
			String choice_str = input.nextLine();
			String[] strs = choice_str.split(",");
			int[] ints = new int[3]; 
			boolean valid = true;
			if(strs.length != 3) {
				System.out.println("Please choose three brawlers!");
				continue;
			}
			for(int i=0; i<3; i++) {
				if(Integer.parseInt(strs[i]) > brawlerList.size() || Integer.parseInt(strs[i]) < 0) {
					valid = false;
					break;
				}
				ints[i] = Integer.parseInt(strs[i])-1;
			}
			if(!valid) {
				System.out.println("Invalid");
				continue;
			}
			for(int i=0; i<ints.length; i++) {
				// use copy constructor to create a new Brawler object
				Brawler bra = new Brawler(brawlerList.get(ints[i]));
				activeList.add(new ActiveBrawler(false, bra, bra.getStats().getHealth()));
			}
			
			System.out.println("These are your Brawlers:");
			for(ActiveBrawler ab : activeList) {
				System.out.println("==" + ab.getBrawler().getName() + "==");
			}
			
			break;
		}
		
		InfoObj pickedInfo = new InfoObj();
		pickedInfo.setInfoType("picked");
		pickedInfo.setActiveList(activeList);
		try {
			oos.writeObject(pickedInfo);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	
 	public int moveInput(InfoObj info) {
 		int abNum = info.getCurBraw().getBrawler().getAbilities().size();
 		int num = 0;
 		while(true) {
 			try {
 		 		Scanner input = new Scanner(System.in);
 	 			num = input.nextInt();
 	 			if(num > abNum || num < 1) {
 					System.out.println("Invalid input, please enter again: ");
 					continue;
 	 			}
 	 			break;
			} catch (Exception e) {
				System.out.println("Enter an interger, please enter again: ");
			}
 		}
 		return num;
 	}
 	
 	
	 public static void main(String[] args) {
		new Client();
	 }
}
