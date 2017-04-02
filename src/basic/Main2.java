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
	public static String lastBlockHash = "000000000000000006c21862a573610a9394024624058d637e4ec3f5cd987774"; //
	public static void main(String[] args) throws JSONException, IOException{
//		ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, true, null, 1);
//		p.parse();
		
		//after getting rid of all "System.exit" and filling in Main2.lastBlockHash
		// not to be used when this is the first parse
		int counter = 4;
		while(true){
			ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, false, Main2.lastBlockHash, counter);
			counter++;
			try{
				p.parse();				
			}catch(java.lang.NullPointerException ne){
				ne.printStackTrace();
				try {
					Thread.sleep(300000);
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