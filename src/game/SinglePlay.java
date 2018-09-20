package game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import model.Ability;
import model.ActiveBrawler;
import model.Brawler;
import model.InfoObj;

public class SinglePlay {
	private ArrayList<Brawler> brawlerList;
	private ArrayList<ActiveBrawler> myActiveList;
	private ArrayList<ActiveBrawler> comActiveList;
	
	private ActiveBrawler myCurActive;
	private ActiveBrawler comCurActive;

	Map<String, Integer> compMap;
	Map<Double, String> multi_effective;
	
	boolean gameover = false;
	
	
	public SinglePlay(ArrayList<Brawler> brawlerList) {
		this.compMap = new HashMap<>();
		compMap.put("water", 1);
		compMap.put("lightning", 2);
		compMap.put("earth", 3);
		compMap.put("air", 4);
		compMap.put("fire", 5);

		this.multi_effective = new HashMap<>();
		multi_effective.put(1.0, "It was effective!");
		multi_effective.put(2.0, "It super effective!");
		multi_effective.put(0.5, "It was not very effective!");
		
		this.brawlerList = brawlerList;
		this.myActiveList = new ArrayList<ActiveBrawler>();
		this.comActiveList = new ArrayList<ActiveBrawler>();
		System.out.println("size of brawlerlist: " + brawlerList.size());
		for(int i=0; i<3; i++) {
			Brawler newBra = new Brawler(brawlerList.get(randomInt(brawlerList.size())));
			comActiveList.add(new ActiveBrawler(false, newBra, newBra.getStats().getHealth() ));
//			System.out.println("computer choose: " + newBra.getName());
		}
		pickBrawler();
		myCurActive = myActiveList.get(0);
		comCurActive = comActiveList.get(0);
		
		playGame();
	}
	
	public void pickBrawler() {
 		Scanner input = new Scanner(System.in);
 		System.out.println("Please choose three brawlers: ");
 		for(int i=0; i<brawlerList.size(); i++) {
 			Brawler br = brawlerList.get(i);
 			System.out.println((i+1) + ") " + br.getName());
 		}
		while(true) {
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
				this.myActiveList.add(new ActiveBrawler(false, bra, bra.getStats().getHealth()));
			}
			
			System.out.println("These are your Brawlers:");
			for(ActiveBrawler ab : this.myActiveList) {
				System.out.println("==" + ab.getBrawler().getName() + "==");
			}
			
			break;
		}
		
	}
	
	public int randomInt(int upperBound) {

		Random rand = new Random();
		int random_integer = rand.nextInt(upperBound);
		return random_integer;
	}
	
	public void playGame() {
		while(true) {
			takeMove();
			
			if(gameover) {
				break;
			}
		}
	}
	
	public void takeMove() {
		// computer chooses a move
		ArrayList<Ability> comAbs = comCurActive.getBrawler().getAbilities();
		Ability comAb = comAbs.get(randomInt(comAbs.size()));
		
		// prompt the user to choose a move
		ArrayList<Ability> myAbs = myCurActive.getBrawler().getAbilities();	
		System.out.println("\nPlease choose a move:");
		for(int i=0; i<myAbs.size(); i++) {
			Ability ab = myAbs.get(i);
			System.out.println((i+1) + ") " + ab.getName() + ", " + ab.getType() + ", " + ab.getDamage());
		}
			
		int abNum = myCurActive.getBrawler().getAbilities().size();
 		int move = 0;
 		while(true) {
 			try {
 		 		Scanner input = new Scanner(System.in);
 	 			move = input.nextInt();
 	 			if(move > abNum || move < 1) {
 					System.out.println("Invalid input, please enter again: ");
 					continue;
 	 			}
 	 			break;
			} catch (Exception e) {
				System.out.println("Enter an interger, please enter again: ");
			}
 		}
 		Ability myAb = myCurActive.getBrawler().getAbilities().get(move - 1);
 		calcDmg(myAb, comAb);
	}
	

	public void calcDmg(Ability myAb, Ability comAb){
//		System.out.println("computer choose "+comAb.getName()+" and you choose "+ myAb.getName());
		System.out.println("You are using " + myCurActive.getBrawler().getName());
		System.out.println("Com is using " + comCurActive.getBrawler().getName());
		
		double mySpeed = this.myCurActive.getBrawler().getStats().getSpeed();
		double myAttack = this.myCurActive.getBrawler().getStats().getAttack();
		double myDefense = this.myCurActive.getBrawler().getStats().getDefense();
		double myAbDmg = myAb.getDamage();
		double myMult = getMult(myAb.getType(), comCurActive.getBrawler().getType());
		int myLiveHealth = myCurActive.getLiveHealth();
		String myEffective = multi_effective.get(myMult);
		
		double comSpeed = this.comCurActive.getBrawler().getStats().getSpeed();
		double comAttack = this.comCurActive.getBrawler().getStats().getAttack();
		double comDefense = this.comCurActive.getBrawler().getStats().getDefense();
		double comAbDmg = comAb.getDamage();
		double comMult = getMult(comAb.getType(), myCurActive.getBrawler().getType());
		int comLiveHealth = comCurActive.getLiveHealth();
		String comEffective = multi_effective.get(comMult);

		int myDmg = (int) (Math.floor((myAttack * (myAbDmg / comDefense)) / 5 * myMult));
		myDmg = myDmg < comLiveHealth? myDmg : comLiveHealth;
		int comDmg = (int) (Math.floor((comAttack * (comAbDmg / myDefense)) / 5 * comMult));
		comDmg = comDmg < myLiveHealth? comDmg : myLiveHealth;
		
		boolean defeated = false;
		
		if(mySpeed >= comSpeed) {
			comCurActive.setLiveHealth(comLiveHealth - myDmg);
			System.out.println(myCurActive.getBrawler().getName() + " used " + myAb.getName() + "\n" + myEffective);
			System.out.println("It did " + myDmg + " damage");
			System.out.println("------you attack first----your opponent has " + comCurActive.getLiveHealth() + "health");
			// check if the computer is still alive
			if(comCurActive.getLiveHealth() > 0) {
				myCurActive.setLiveHealth(myLiveHealth - comDmg);
				System.out.println(comCurActive.getBrawler().getName() + " used " + comAb.getName() + "\n" + comEffective);
				System.out.println("It did " + comDmg + " damage");
				if(myCurActive.getLiveHealth() == 0) {
					defeated = true;
					System.out.println(myCurActive.getBrawler().getName() + " was defeated ");
					getNewBr("you", myActiveList);
				}
			}
			else {
				System.out.println(comCurActive.getBrawler().getName() + " was defeated");
				defeated = true;
				getNewBr("com", comActiveList);
			}
			System.out.println(myCurActive.getBrawler().getName() + " has " + myCurActive.getLiveHealth());
			System.out.println(comCurActive.getBrawler().getName() + " has " + comCurActive.getLiveHealth());
		}else {
			myCurActive.setLiveHealth(myLiveHealth - comDmg);
			System.out.println(comCurActive.getBrawler().getName() + " used " + comAb.getName() + "\n" + comEffective);
			System.out.println("It did " + comDmg + " damage");
			// check if you are still alive
			if(myCurActive.getLiveHealth() > 0) {
				comCurActive.setLiveHealth(comLiveHealth - myDmg);
				System.out.println(myCurActive.getBrawler().getName() + " used " + myAb.getName() + "\n" + myEffective);
				System.out.println("It did " + myDmg + " damage");
				if(comCurActive.getLiveHealth() == 0) {
					defeated = true;
					System.out.println(comCurActive.getBrawler().getName() + " was defeated ");
					getNewBr("com", comActiveList);
				}
			}
			else {
				System.out.println(myCurActive.getBrawler().getName() + " was defeated");
				defeated = true;
				getNewBr("you", myActiveList);
			}
			if(myCurActive.getLiveHealth() == myCurActive.getBrawler().getStats().getHealth()) {
				System.out.println(myCurActive.getBrawler().getName() + " has " + myCurActive.getLiveHealth() + " health");
			}
//			System.out.println(comCurActive.getBrawler().getName() + " has " + comCurActive.getLiveHealth());
		}
		
	}
	
	public double getMult(String selfAbType, String opBrType) {
		double multiplier = 1;
		if(compMap.get(selfAbType) - compMap.get(opBrType) == 1 
				|| compMap.get(selfAbType) - compMap.get(opBrType) == -4) {
			multiplier = 2;
		}
		if(compMap.get(selfAbType) - compMap.get(opBrType) == -1 
				|| compMap.get(selfAbType) - compMap.get(opBrType) == 4) {
			multiplier = 0.5;
		}
		return multiplier;
	}
	
	public void getNewBr(String user, ArrayList<ActiveBrawler> activeList) {
		System.out.println("---------Getting new brawler------------");
		for(ActiveBrawler newBr : activeList) {
			if(newBr.getLiveHealth() > 0) {
				if(user.equals("you")) {
					this.myCurActive = newBr;
					System.out.println(user + " send out " + this.myCurActive.getBrawler().getName());
				}
				else {
					this.comCurActive = newBr;
					System.out.println(user + " send out " + this.myCurActive.getBrawler().getName());
				}
				break;
			}
		}
		if(this.comCurActive.getLiveHealth() == 0 || this.myCurActive.getLiveHealth() == 0) {
			gameover(user);
		}
	}
	
	public void gameover(String loser) {
		if(loser.equals("com")) {
			System.out.println("You win!");
		}
		else {
			System.out.println("You lose!");
		}
		this.gameover = true;
	}
}










