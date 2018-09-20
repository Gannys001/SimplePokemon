package model;

import java.io.Serializable;

public class ActiveBrawler implements Serializable{
	private boolean dead;
	private Brawler brawler;
	private int liveHealth;
	
	public ActiveBrawler() {}

	public ActiveBrawler(boolean dead, Brawler brawler, int liveHealth) {
		super();
		this.dead = dead;
		this.brawler = brawler;
		this.liveHealth = liveHealth;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public Brawler getBrawler() {
		return brawler;
	}

	public void setBrawler(Brawler brawler) {
		this.brawler = brawler;
	}

	public int getLiveHealth() {
		return liveHealth;
	}

	public void setLiveHealth(int liveHealth) {
		this.liveHealth = liveHealth;
	}
	
}
