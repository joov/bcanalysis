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
	public static String lastTranHash; //
	public static String lastBlockHash; //
	public static String currTranHash;
	public static LastAddr lastAddr;
	public static void main(String[] args) throws JSONException, IOException{
//		ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, true, null, 1);
//		p.parse();

		// Set Logger
		Logger LOG = Logger.getLogger(Main3.class) ;
		
		BCProperties.loadProperties() ;

		Main3.lastBlockHash = BCProperties.getProperty("last.block", "00000000000000000045792345e2e6506db04f7d6511de933d5a5e5c3127199d") ;
		Main3.lastTranHash = BCProperties.getProperty("last.trans", "0531bd4da288268a22ed41a8cf7eb1ae76c91955e4a7060e02770bb0b7c196c0") ;
		Main3.currTranHash = BCProperties.getProperty("curr.trans", "0e5d7643b611cca88297a60d1c52567cfda919b7b4f45fce31de6e635ba53269") ;
		String lastAddrID = BCProperties.getProperty("last.addr", "14GKDnmbBwBtH3bF7tuDWCJi4Ey3wgkbQL") ;
		boolean isOutput = Boolean.parseBoolean(BCProperties.getProperty("isOutput", "true"));
		int outputIndex = Integer.parseInt(BCProperties.getProperty("outputIndex", "166"));
		
		Main3.lastAddr = new LastAddr(lastAddrID, Main3.currTranHash, isOutput, outputIndex)
		
		
		int counter = Integer.parseInt(BCProperties.getProperty("main.counter", "16")); //also sometimes need to be changed
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