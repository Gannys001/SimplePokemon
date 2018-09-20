package model;

import java.util.ArrayList;

public class Brawlers {
	ArrayList<Brawler> Brawlers;
	public Brawlers() {
		
	}
	public Brawlers(ArrayList<Brawler> brawlers) {
		super();
		this.Brawlers = brawlers;
	}
	// copy constructor
	public Brawlers(Brawlers oldBrawlers) {
		ArrayList<Brawler> oldList = oldBrawlers.getBrawlers();
		this.Brawlers = new ArrayList<Brawler>();
		for(Brawler oldBrlr : oldList) {
			Stats oldStats = oldBrlr.getStats();
			ArrayList<Ability> oldAbilities = oldBrlr.getAbilities();
			ArrayList<Ability> newAbilities = new ArrayList<Ability>();
			for(Ability ab : oldAbilities) {
				Ability newAbility = new Ability(ab.getName(), ab.getType(), ab.getDamage());
				newAbilities.add(newAbility);
			}
			this.Brawlers.add(new Brawler(oldBrlr.getName(), oldBrlr.getType(), 
								new Stats(oldStats.getHealth(), oldStats.getAttack(), oldStats.getDefense(), oldStats.getSpeed()), 
									newAbilities));
		}
	}
	
	
	public ArrayList<Brawler> getBrawlers() {
		return Brawlers;
	}
	public void setBrawlers(ArrayList<Brawler> brawlers) {
		this.Brawlers = brawlers;
	}
	
}
