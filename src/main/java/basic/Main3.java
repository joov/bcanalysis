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
	public static String lastTranHash = "d574cc8d4f962f534d2540da67a7dbc73b30ead3af34fac02434e53f312e6af9"; //
	public static String lastBlockHash = "0000000000000000055166d31041a3068167eb3c9f9ab16ee04f9ddd77140c93"; //
	public static String currTranHash = "92088fe6b9d9c9db84d26481b80104f8ca566649a656b7aa3e67432b346cbf1c";
	public static String lastAddr = "18a53xQhYFoBu1jxVAFbqp1SgzPqPM8aEd";
	public static int counter = 153;
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