package basic;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

/**
 * This class will is a super class for the classes which extract 
 * data of bitcoin transaction from blocktrail api and blockchain api
 * according to Model 2.
 * @author yshi
 *
 */
public class ParserToCSVDateModel2 extends ParserToCSVModel2 implements BitCoinExRateGetterDay{	
	private Map<String, Double> timeExchangeRate = new HashMap<String, Double>();

	
	public ParserToCSVDateModel2(int numBlock, boolean begin, String lastBlock, 
			String lastTran, String currTran, String lastAddr, int folderCounter) throws FileNotFoundException {
		super(numBlock, begin, lastBlock, lastTran, currTran, lastAddr, folderCounter);
	}
	
	protected double getDollarValDayorHour(String time, String value) throws JSONException, IOException{
		return this.getDollarValDay(time, value);
	}
	
	/* (non-Javadoc)
	 * @see basic.BitCoinExRateGetterDay#getDollarValDay(java.lang.String, java.lang.String)
	 */
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public double getDollarValDay(String time, String value) throws IOException, JSONException{
		double val = (double)(Long.parseLong(value));

		String target = time.split("\\+")[0];
		String date = target.split("T")[0];
	    double dollar = 0.0;
		if(this.timeExchangeRate.containsKey(date)){
			dollar = this.timeExchangeRate.get(date);
	    }else{
		    JSONObject rateJson = readJsonFromUrl("https://api.coindesk.com/v1/bpi/historical/close.json?start=" 
		    	    + date + "&end=" + date);
		    JSONObject bpi = rateJson.getJSONObject("bpi");
		    dollar = bpi.getDouble(date);
			this.timeExchangeRate.put(date, new Double(dollar));
	    }

//		System.out.println("time");
//		System.out.println(time);
//		System.out.println(rateJson.get("timestamp"));	
			    
		return val*BitCoinExRateGetterDay.SATTOBIT*dollar;
	}

}
