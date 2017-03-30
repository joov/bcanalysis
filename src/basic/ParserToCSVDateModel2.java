package basic;

import java.io.*;
import java.util.ArrayList;

import org.json.*;

/**
 * This class will is a super class for the classes which extract 
 * data of bitcoin transaction from blocktrail api and blockchain api
 * according to Model 2.
 * @author yshi
 *
 */
public class ParserToCSVDateModel2 extends ParserToCSVModel2 implements BitCoinExRateGetterDay{	
	
	public ParserToCSVDateModel2(ArrayList<String> datFileNames) throws FileNotFoundException {
		super(datFileNames);
	}
	
	protected double getDollarValDayorHour(String time, String value) throws JSONException, IOException{
		return this.getDollarValDay(time, value);
	}
	
	/* (non-Javadoc)
	 * @see basic.BitCoinExRateGetterDay#getDollarValDay(java.lang.String, java.lang.String)
	 */
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public double getDollarValDay(String time, String value) throws IOException, JSONException{
		double val = Long.parseLong(value);
		String target = time.split("\\+")[0];
		String date = target.split("T")[0];
	    JSONObject rateJson = readJsonFromUrl("https://api.coindesk.com/v1/bpi/historical/close.json?start=" 
	    + date + "&end=" + date);
	    JSONObject bpi = rateJson.getJSONObject("bpi");
	    double dollar = bpi.getDouble(date);
		System.out.println("time");
		System.out.println(time);
		System.out.println(rateJson.get("timestamp"));		
		return val*BitCoinExRateGetterDay.SATTOBIT*dollar;
	}

}
