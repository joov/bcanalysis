package basic;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.json.*;


/**
 * This class will extract data regarding bitcoin transaction
 * from blocktrail api and blockchain api
 * The exchange rate between bitcoin is obtained from a
 * website which has hourly exchange rate available
 * @author yshi
 *
 */
public class ParserToCSVHourModel2 extends ParserToCSVModel2 implements BitCoinExRateGetterHour{		
	private Map<Long, Double> timeExchangeRate = new HashMap<Long, Double>();

	
	public ParserToCSVHourModel2(int numBlock, boolean begin, String lastBlock, String lastTran, int folderCounter) throws FileNotFoundException {
		super(numBlock, begin, lastBlock, lastTran, folderCounter);
	}
	
	protected double getDollarValDayorHour(String time, String value) throws JSONException, IOException{
		return this.getDollarValHour(time, value);
	}

	/* (non-Javadoc)
	 * @see basic.BitCoinExRateGetterDay#getDollarValDay(java.lang.String, java.lang.String)
	 */
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public double getDollarValHour(String time, String value) throws IOException, JSONException{
		double val = (double)(Long.parseLong(value));
	    
	    long timePara = 0;
	    try {
			timePara = Util.getLongTimeClosestMinute(time)/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	    double penny = 0.0;
	    if(this.timeExchangeRate.containsKey(new Long(timePara))){
	    	penny = this.timeExchangeRate.get(new Long(timePara));
	    }else{
		    JSONObject rateJson = this.readJsonFromUrl("https://winkdex.com/api/v0/price?time=" + timePara);
			penny = (double)(rateJson.getInt("price"));
			this.timeExchangeRate.put(new Long(timePara), new Double(penny));
	    }

		return val*BitCoinExRateGetterHour.SATTOBIT*penny/100.0;
	}
}
