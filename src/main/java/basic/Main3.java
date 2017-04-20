package basic;

import java.io.IOException;
import org.json.JSONException;

/**
 * The class containing a main method to use the parser classes for model 2
 * 
 * @author yshi
 *
 */
public class Main3 {
	
	//to be change to the output of the String in the output after "Last Hash: " in the last parse
	public static String lastTranHash = "d545ba112d375aa31b5652fd5ba2657673cfc622cb4b53a0fbe06c54145656b5"; //
	public static String lastBlockHash = "000000000000000000e731b8e81fccc3168402de0ed0d4066c85920f86ae1913"; //
	public static String currTranHash = "94cdebb2c44db4969824c79468358f9ec555bb9a2c0d12d875d56d31452916b7";
	public static LastAddr lastAddr = new LastAddr("1AV8Ym2s8Jk9wQa2zATCv4nxTHYd1ppWpe", "94cdebb2c44db4969824c79468358f9ec555bb9a2c0d12d875d56d31452916b7", true, 227);
	public static int counter = 10093; //10016
	public static void main(String[] args) throws JSONException, IOException{
//		ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, true, null, 1);
//		p.parse();
		while(true){
			System.out.println(counter);
			ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, false, Main3.lastBlockHash, 
					Main3.lastTranHash, Main3.currTranHash, Main3.lastAddr, Main3.counter);
			Main3.counter++;
			try{
				p.parse();				
			}catch(Exception ne){
				ne.printStackTrace();
				try{
					p.end();					
				}catch(Exception e){
					try {
						Thread.sleep(600000);  
					} catch (InterruptedException iE) {
						e.printStackTrace();
					}
				}
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
