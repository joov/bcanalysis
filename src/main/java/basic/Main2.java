package basic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * The class containing a main method to use the parser classes for model 2
 * 
 * @author yshi
 *
 */
public class Main2 {
	
	private static Properties properties = new Properties() ;
	
	/**
	 * Get main properties.
	 * @return properties
	 */
	public static Properties getProperties() {
		return properties ;
	}
	public static void main(String[] args) throws JSONException, IOException{
//		ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, true, null, 1);
//		p.parse();

		// Set Logger
		Logger LOG = Logger.getLogger(Main2.class) ;
		
		// Load Properties
		String fileName = System.getProperty("properties.file", System.getProperty("user.dir") + "/bcanalysis.properties") ;
		LOG.debug("Reading properties from " + fileName);

		InputStream is =  new FileInputStream(fileName);
		if (is != null) {
			LOG.debug("Properties file found");
			properties.load(is);
			is.close();
		}
		else {
			LOG.debug("No properties file found. Continuing with defaults");
		}

		String lastBlockHash = getProperties().getProperty("last.block", "0000000000000000061338c784fa43a7fce7d3fe671e4d79e06fb8de704da30f") ;
		String lastTranHash = getProperties().getProperty("last.trans", "20c8598e3597bd51d325b4f69d3673fc336ebb838fd77fe9050179f2cd27fda1") ;
		String currTranHash = getProperties().getProperty("curr.trans", "11474b5c73ed018bb09cc8647e12212eec1e0bd32b45fa444dfc479b68c2cc0f") ;
		String lastAddr = getProperties().getProperty("last.addr", "12hLCoGF2bRr7TmFeS3Pr5zASYqfRXoKgj") ;

		
		int counter = Integer.parseInt(getProperties().getProperty("main.counter", "16")); //also sometimes need to be changed
		
		while(true){
			LOG.debug("Counter: " + counter);
			ParserToCSVHourModel2 p = new ParserToCSVHourModel2(30, false, lastBlockHash, 
					lastTranHash, currTranHash, lastAddr, counter);
			counter++;
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