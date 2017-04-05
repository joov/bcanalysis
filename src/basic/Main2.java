package basic;

import java.io.IOException;
import org.json.JSONException;

/**
 * The class containing a main method to use the parser classes for model 2
 * 
 * @author yshi
 *
 */
public class Main2 {
	
	//to be change to the output of the String in the output after "Last Hash: " in the last parse
	public static String lastBlockHash = "000000000000000008016b21be06e89206d2aa9d2849606f2ce07129a1bde0a5"; //
	public static String lastTranHash = "20c8598e3597bd51d325b4f69d3673fc336ebb838fd77fe9050179f2cd27fda1"; //
	
	public static void main(String[] args) throws JSONException, IOException{
//		ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, true, null, 1);
//		p.parse();
		
		int counter = 16; //also sometimes need to be changed
		while(true){
			System.out.println(counter);
			ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, false, Main2.lastBlockHash, Main2.lastTranHash, counter);
			counter++;
			try{
				p.parse();				
			}catch(java.lang.NullPointerException ne){
				ne.printStackTrace();
				try {
					Thread.sleep(600000);  //increase the sleeping time, if the program is not able to finish off one 
					//block before writing to csv files (just open the last few csv files generated (the files 
					//are in the folder named in csv + a number. So the last few csv files are
					//the ones in the folders with name containing the biggest number (such as csv84)), if the 
					//address.csv has around 300 entries or less, or has more "null" entries than non-null entries in the sixth colum "primWallAdd", 
					//then it is quite likely that the program is not able to finish off one 
					//block before writing to csv files)
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}