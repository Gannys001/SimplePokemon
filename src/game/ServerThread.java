package game;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator.OfDouble;

import javax.sound.midi.MidiDevice.Info;

import model.Ability;
import model.ActiveBrawler;
import model.Brawler;
import model.Brawlers;
import model.InfoObj;

public class ServerThread extends Thread {
	private Socket socket;
	private GameServer gameServer;
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	

	// if the room is full
	private boolean full;
	// the designed number of players in the room
	private int playerNum;
	// name of the room the client is in 
	private String roomName;
	

	Map<String, Integer> compMap;
	
	private ArrayList<ActiveBrawler> activeList;
	
	private ActiveBrawler curBraw;
	
	private int curMove;
	
	
	// state:
	// 1: reading basic info
	// 2. playing game
	private int state = 1;
	
	public ServerThread(Socket socket, GameServer gameServer, String roomeName) {
		this.full = false;

		this.compMap = new HashMap<>();
		compMap.put("water", 1);
		compMap.put("lightning", 2);
		compMap.put("earth", 3);
		compMap.put("air", 4);
		compMap.put("fire", 5);
		
		this.activeList = null;
		this.curBraw = null;
		this.curMove = 0;
		
		this.socket = socket;
		this.gameServer = gameServer;
		this.roomName = roomeName;
		try {
			this.oos = new ObjectOutputStream(socket.getOutputStream());
			this.ois = new ObjectInputStream(socket.getInputStream());
			
			InfoObj info = new InfoObj();
			info.setRoomMap(gameServer.getFullMap());
			info.setInfoType("start");
			oos.writeObject(info);
			oos.flush();
			
			this.start();
		} catch (IOException e) {
			System.out.println("ioe in ServerThread constructors: " + e.getMessage());
			gameServer.removeServerThread(this);
		}
	}
	
	public void run() {
		try {
			while(true) {
				// cannot print to the console here, have to user gameServer to print stuff
				InfoObj info = (InfoObj) ois.readObject();
				String infoType = info.getInfoType();
				if(infoType.equals("new") || infoType.equals("join")) {
					iniGame(info);
				}
				if(this.playerNum==2) {
					if(infoType.equals("prepared")) {
						gameServer.serverLog("The user is preapred");
						chooseBrawler();
					}
					else if(infoType.equals("picked")) {
						gameServer.serverLog("The user picked");
						// wait for both user to finish picking
						waitToStart(info);
						checkNewBraw();
						// for testing
//						activeList.get(0).setLiveHealth(0);
//						activeList.get(1).setLiveHealth(0);
						
						promptMove();
					}
					else if (infoType.equals("moved")) {
						gameServer.serverLog("one move: " + info.getMove());
						calcDmg(info.getMove());
					}
					else if(infoType.equals("requestRes")) {
						checkDeath();
					}
					
					
					
					
				}
				else {
					
				}
			}
		} catch (IOException e) {
			System.out.println("ioe in ServerThread run()" + e.getMessage());
//			e.printStackTrace();
			gameServer.removeServerThread(this);
		} catch (Exception e) {
			System.out.println("e in serverThreade run()" + e.getMessage());
		} finally {
			try {
				oos.close();
				ois.close();
				socket.close();
			} catch (Exception e2) {
				// TODO: handle exception
				System.out.println("exception in serverThread finally");
//				e2.printStackTrace();
			}
		}
	}
	
	public void iniGame(InfoObj info) {

		// if the players wants to start a new game 
		if(info.getInfoType().equals("new")) {
			this.playerNum = info.getPlayerNum();
			this.roomName = info.getRoomName();
			this.full = false;
			
			if(this.playerNum == 2) {		// mark the room as not full
				gameServer.getFullMap().put(roomName, false);
				InfoObj waitInfo = new InfoObj();
				
				waitInfo.setInfoType("message");
				waitInfo.setMessage("waiting for players to connect... ");
				sendMessage(waitInfo);
			}
			else {		// the room is marked as full if the user play with computer
				gameServer.getFullMap().put(roomName, true);
				
				InfoObj singleInfo = new InfoObj();
				singleInfo.setInfoType("single");
				singleInfo.setMessage("Play with the computer!");
				singleInfo.setBrawlerList(gameServer.getBrawlerList());
				sendMessage(singleInfo);
			}
			
		}
		
		else {			// the player wants to join an existing game
			this.playerNum = info.getPlayerNum();
			this.roomName = info.getRoomName();
			
			// mark the room as full
			gameServer.getFullMap().put(roomName, true);
			
			// notify the user that player2 is connected
			InfoObj connectInfo = new InfoObj();
			connectInfo.setInfoType("begin");
			connectInfo.setMessage("player 2 is connected \n------------------Start Game--------------------");
			gameServer.broadcast(connectInfo, this.roomName);
		}
	}
	
	public void chooseBrawler() {
		
		InfoObj brawlerInfo = new InfoObj();
		ArrayList<Brawler> brawlerList = gameServer.getBrawlerList();
		brawlerInfo.setInfoType("pick");
		brawlerInfo.setBrawlerList(brawlerList);
		String msg = "Choose 3 Brawlers: \n";
		for(int i=0; i<brawlerList.size(); i++) {
			msg += (i+1) + ") " + brawlerList.get(i).getName() + "\n";
		}
		brawlerInfo.setMessage(msg);
		
		sendMessage(brawlerInfo);
	}
	
	public void waitToStart(InfoObj playerInfo) {
		this.activeList = playerInfo.getActiveList();
		this.curBraw = activeList.get(0);
		// loop until both players decided their brawler lists
		while(gameServer.getOpponent(this.roomName, this).getActiveList() == null
				|| gameServer.getOpponent(this.roomName, this).getCurBraw() == null) {
			
		}
		this.curBraw = activeList.get(0);
		gameServer.serverLog("Both server has picked!!");
	}
	
	public void checkNewBraw() {
		for(ActiveBrawler ab : this.activeList) {
			if(ab.getLiveHealth()>0) {
				this.curBraw = ab;
				break;
			}
		}
		
		for(ActiveBrawler opAb : gameServer.getOpponent(this.roomName, this).getActiveList()) {
			if(opAb.getLiveHealth()>0) {
				gameServer.getOpponent(this.roomName, this).curBraw = opAb;
				break;
			}
		}
		
		String self_str = "";
		String opp_str = "";
		String final_str = "";
		// check if your current brawler is a newly sent one
		if(curBraw.getLiveHealth() == curBraw.getBrawler().getStats().getHealth()) {
			self_str = "You send out " + curBraw.getBrawler().getName();
		}
		// check if your opponent send a new one
		ServerThread oppThread = gameServer.getOpponent(this.roomName, this);
		if(oppThread.getCurBraw().getLiveHealth() == oppThread.getCurBraw().getBrawler().getStats().getHealth()) {
			opp_str = "Your oppnent send out " + oppThread.getCurBraw().getBrawler().getName();
		}
		final_str = self_str + "\n" + opp_str + "\n";
		
		InfoObj newBrawInfo = new InfoObj();
		newBrawInfo.setInfoType("message");
		newBrawInfo.setMessage(final_str);
		sendMessage(newBrawInfo);
		
		
	}
	
	public void promptMove() {
		InfoObj promptInfo = new InfoObj();
		promptInfo.setInfoType("move");
		promptInfo.setCurBraw(this.curBraw);
		
		String ablt_str = "Choose a move: \n";
		ArrayList<Ability> abilities = curBraw.getBrawler().getAbilities();
		for(int i=0; i<abilities.size(); i++) {
			Ability ability = abilities.get(i);
			ablt_str += (i+1) + ")" + ability.getName() + ", "
					+ ability.getType() + ", " + ability.getDamage() + "\n";
		}
		gameServer.serverLog("prompt " + curBraw.getBrawler().getName() + " to move");
		promptInfo.setMessage(ablt_str);
		sendMessage(promptInfo);
	}
	
	// executed by both server threads
	public void calcDmg(int move) {
		this.curMove = move;
		// wait until the other user choose a move
		while(gameServer.getOpponent(this.roomName, this).getCurMove() == 0) {
			
		}
		ArrayList<Ability> selfAbs = curBraw.getBrawler().getAbilities();
		int myIdx = this.curMove - 1;
		
		ActiveBrawler opActiveBraw = gameServer.getOpponent(this.roomName, this).getCurBraw();
		String selfAbType = selfAbs.get(myIdx).getType();
		String oppBrawType = opActiveBraw.getBrawler().getType();

		double multiplier = 1;
		String effective = "It was effective!";
		if(compMap.get(selfAbType) - compMap.get(oppBrawType) == 1 
				|| compMap.get(selfAbType) - compMap.get(oppBrawType) == -4) {
			multiplier = 2;
			effective = "It was super effective!";
		}
		if(compMap.get(selfAbType) - compMap.get(oppBrawType) == -1 
				|| compMap.get(selfAbType) - compMap.get(oppBrawType) == 4) {
			multiplier = 0.5;
			effective = "It was not very effective!";
		}
		
		double attackStat = this.curBraw.getBrawler().getStats().getAttack();
		double abilityDamage = selfAbs.get(myIdx).getDamage();
		double defenseStat = opActiveBraw.getBrawler().getStats().getDefense();
		double original = (attackStat * (abilityDamage / defenseStat)) / 5 * multiplier;
		int damage = (int) (Math.floor((attackStat * (abilityDamage / defenseStat)) / 5 * multiplier));
		if(damage > opActiveBraw.getLiveHealth()) {
			damage = opActiveBraw.getLiveHealth();
		}
		gameServer.serverLog(curBraw.getBrawler().getName()+"--"+attackStat+"--"
				+abilityDamage+"--"+defenseStat+"--"+multiplier+"--"+original);
		int selfSpeed = curBraw.getBrawler().getStats().getSpeed();
		int opSpeed = opActiveBraw.getBrawler().getStats().getSpeed();
		
		InfoObj dmgInfo = new InfoObj();
		dmgInfo.setInfoType("calculated");
		dmgInfo.setMessage(curBraw.getBrawler().getName() + " used " + selfAbs.get(myIdx).getName() + "\n"
				+ effective + "\n" + "It did " + damage + " damage");
		
		if(selfSpeed > opSpeed) {
			opActiveBraw.setLiveHealth(opActiveBraw.getLiveHealth() - damage);
			gameServer.serverLog("first calculated from "+curBraw.getBrawler().getName()+" damage--" + damage + 
					" oldHealth " + opActiveBraw.getLiveHealth());
			gameServer.broadcast(dmgInfo, this.roomName);
		}
		else {
			//wait until the other user finish calculating and sending information to clients
			while(gameServer.getOpponent(this.roomName, this).getCurMove() != 0) {
				
			}
			// attack if the slower brawler is alive
			if(this.curBraw.getLiveHealth() != 0) {
				opActiveBraw.setLiveHealth(opActiveBraw.getLiveHealth() - damage);
				gameServer.broadcast(dmgInfo, this.roomName);
				
				gameServer.serverLog("secon calculated from "+curBraw.getBrawler().getName()+" damage--" + damage + 
						" oldHealth " + opActiveBraw.getLiveHealth());
			}
			else {
				gameServer.serverLog(curBraw.getBrawler().getName() + " is slower and defeated");
			}
			checkDeath();
		}
		curMove = 0;
	}
	
	// done by the slower one
	public void checkDeath() {
		String curName = curBraw.getBrawler().getName();
		int curHealth = curBraw.getLiveHealth();
		String leftHealth = "";
		
		boolean selfDefeated = false;
		String selfDefeated_str = "";
		boolean opDefeated = false;
		String opDefeated_str = "";
		
		if(curBraw.getLiveHealth() != 0) {
			leftHealth = curBraw.getBrawler().getName() + " has " + curHealth + " health " + "\n";
		}
		else {
			leftHealth = curName + " was defeated";
			selfDefeated = true;
			selfDefeated_str = leftHealth;
		}
		
		// get the health condition of opponent 
		ActiveBrawler opBrawler = gameServer.getOpponent(this.roomName, this).getCurBraw();
		String opHelath = "";
		if(opBrawler.getLiveHealth() !=0 ) {
			opHelath = opBrawler.getBrawler().getName() + " has " + opBrawler.getLiveHealth() + " health " + "\n";
		}
		else {
			opHelath = opBrawler.getBrawler().getName() + " was defeated";
			opDefeated_str = opHelath;
			opDefeated = true;
		}

		gameServer.serverLog(leftHealth + "\n" + opHelath);
		
		// send attack results to users
		InfoObj selfRes = new InfoObj();
		selfRes.setInfoType("results");
		if(opDefeated) {
			selfRes.setMessage(leftHealth + opDefeated_str);
		}
		else {
			selfRes.setMessage(leftHealth);
		}
		sendMessage(selfRes);
		
		// to opponent
		InfoObj opRes = new InfoObj();
		opRes.setInfoType("results");
		if(selfDefeated) {
			opRes.setMessage(opHelath + selfDefeated_str);
		}
		else {
			opRes.setMessage(opHelath);
		}
		gameServer.sendToOpponent(opRes, this.roomName, this);
		
		// check if game is over
		if(selfDefeated || opDefeated) {
			gameOver();
		}
		
		// check if a new brawler if needed
		checkNewBraw();
		gameServer.getOpponent(this.roomName, this).checkNewBraw();
		
		// prompt both user to make next move
		promptMove();
		gameServer.getOpponent(this.roomName, this).promptMove();
		
	}
	
	public void gameOver() {
		InfoObj loseInfo = new InfoObj();
		InfoObj winInfo = new InfoObj();
		loseInfo.setInfoType("gameover");
		loseInfo.setMessage("You are out of brawlers!\nYou lose!");
		winInfo.setInfoType("gameover");
		winInfo.setMessage("Your opponent is out of brawlers!\nYou win!");
		
		ActiveBrawler opCurBraw = gameServer.getOpponent(this.roomName, this).getCurBraw();
		
		if(curBraw.getLiveHealth() == 0 && curBraw == activeList.get(2)) {
			sendMessage(loseInfo);
			gameServer.sendToOpponent(winInfo, this.roomName, this);
		}
		if(opCurBraw.getLiveHealth() == 0 && opCurBraw == gameServer.getOpponent(this.roomName, this).getActiveList().get(2)) {
			sendMessage(winInfo);
			gameServer.sendToOpponent(loseInfo, this.roomName, this);
		}
	}
	
	public void sendMessage(InfoObj info) {
		try {
			oos.writeObject(info);
			oos.flush();
		} catch (IOException e) {
			System.out.println("ioe in ServerThread sendMessage(): " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("e in ServerThread sendMessage: " + e.getMessage());
			e.printStackTrace();
		}
	
	}
	
	
	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public ActiveBrawler getCurBraw() {
		return curBraw;
	}

	public void setCurBraw(ActiveBrawler curBraw) {
		this.curBraw = curBraw;
	}

	public ArrayList<ActiveBrawler> getActiveList() {
		return activeList;
	}

	public void setActiveList(ArrayList<ActiveBrawler> activeList) {
		this.activeList = activeList;
	}

	public int getCurMove() {
		return curMove;
	}

	public void setCurMove(int curMove) {
		this.curMove = curMove;
	}
	
}
