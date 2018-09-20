package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import com.google.gson.Gson;

import model.Brawler;
import model.Brawlers;

public class Test {

	public static void main(String[] args) {
		FileReader fReader;
		Gson gson = new Gson();
//		System.out.println("Please enter a valid file:");
		ArrayList<Brawler> brawlerList= new ArrayList<Brawler>();
		try {
			fReader = new FileReader("sample.json");
			Brawlers brawlers = gson.fromJson(fReader, Brawlers.class);
			brawlerList = brawlers.getBrawlers();
			Brawler b1 = brawlerList.get(0);
//			System.out.println(brawlerList.get(0).getStats().getAttack());
			
//			Brawler brawler_copy = new Brawler(b1);
//			System.out.println("the brawler_copy: "+ brawler_copy.getName() + "---------" + brawler_copy.getAbilities().get(0).getName());
		
			Brawlers bs_copy  = new Brawlers(brawlers);
			ArrayList<Brawler> copyList = bs_copy.getBrawlers();
			System.out.println("from the copy obj");
			System.out.println(copyList.size());
			Brawler copy1 = copyList.get(1);
			System.out.println(copy1.getName() + "---"+copy1.getType()+"---"+copy1.getAbilities().get(0).getType());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}