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
	public static String lastBlockHash = "0000000000000000012f0d787bfcf78b78d6ea83fbe9354844717281e5ba485a"; //
	
	public static void main(String[] args) throws JSONException, IOException{
//		ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, true, null, 1);
//		p.parse();
		
		int counter = 9;
		while(true){
			ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, false, Main2.lastBlockHash, counter);
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