package model;

import java.awt.SecondaryLoop;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import game.ServerThread;

public class InfoObj implements Serializable {
	
	// info type: 0.room information 1. start signal 1. user choice 2.brawler sent 3.move made 4.result 5. 
	
	public static final long serialVersionUID = 1;
	private String infoType;	
	
	// when receiving type "start"
	// a. if start new game, send with type "new"
	// b. if join a game, send with type "join"
	private int playerNum;
	private String roomName; 
	
	private Map<String, Boolean> roomMap;
	
	private String message;
	
	private ArrayList<Brawler> brawlerList;
	private ArrayList<ServerThread> serverThreads;
	
	private ArrayList<ActiveBrawler> activeList;
	private ActiveBrawler curBraw;
	
	private int move;
	
	public InfoObj() {}

	public String getInfoType() {
		return infoType;
	}

	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}

	public ArrayList<Brawler> getBrawlerList() {
		return brawlerList;
	}

	public void setBrawlerList(ArrayList<Brawler> brawlerList) {
		this.brawlerList = brawlerList;
	}

	public ArrayList<ServerThread> getServerThreads() {
		return serverThreads;
	}

	public void setServerThreads(ArrayList<ServerThread> serverThreads) {
		this.serverThreads = serverThreads;
	}

	public int getMove() {
		return move;
	}

	public void setMove(int move) {
		this.move = move;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public void setPlayerNum(int playerNum) {
		this.playerNum = playerNum;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, Boolean> getRoomMap() {
		return roomMap;
	}

	public void setRoomMap(Map<String, Boolean> roomMap) {
		this.roomMap = roomMap;
	}

	public ArrayList<ActiveBrawler> getActiveList() {
		return activeList;
	}

	public void setActiveList(ArrayList<ActiveBrawler> activeList) {
		this.activeList = activeList;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ActiveBrawler getCurBraw() {
		return curBraw;
	}

	public void setCurBraw(ActiveBrawler curBraw) {
		this.curBraw = curBraw;
	}

	
}
