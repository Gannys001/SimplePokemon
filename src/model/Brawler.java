package model;

import java.io.Serializable;
import java.util.ArrayList;

import javax.net.ssl.SSLEngineResult.Status;

public class Brawler implements Serializable{

	String name;
	String type;
	Stats stats;
	ArrayList<Ability> abilities;
	public Brawler() {
		
	}
	public Brawler(String name, String type, Stats stat, ArrayList<Ability> abilities) {
		super();
		this.name = name;
		this.type = type;
		this.stats = stat;
		this.abilities = abilities;
	}
	
	// copy constructor
	public Brawler(Brawler brawler) {
		this.name = brawler.getName();
		this.type = brawler.getType();
		
		Stats oldStats = brawler.getStats();
		this.stats = new Stats(oldStats.getHealth(), oldStats.getAttack(), oldStats.getDefense(), oldStats.getSpeed());
		
		this.abilities = new ArrayList<Ability>();
		ArrayList<Ability> oldAbilities = brawler.getAbilities();
		for(Ability ab : oldAbilities) {
			Ability newAbility = new Ability(ab.getName(), ab.getType(), ab.getDamage());
			this.abilities.add(newAbility);
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Stats getStats() {
		return stats;
	}
	public void setStats(Stats stat) {
		this.stats = stat;
	}
	public ArrayList<Ability> getAbilities() {
		return abilities;
	}
	public void setAbilities(ArrayList<Ability> abilities) {
		this.abilities = abilities;
	}
	
}
