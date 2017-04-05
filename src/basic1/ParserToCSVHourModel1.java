package basic1;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;


import org.json.*;

import basic.BitCoinExRateGetterHour;
import basic.Util;


/**
 * This class will extract data regarding bitcoin transaction
 * from blocktrail api and blockchain api
 * The exchange rate between bitcoin is obtained from a
 * website which has hourly exchange rate available
 * @author yshi
 *
 */
public class ParserToCSVHourModel1 extends ParserToCSVModel1 implements BitCoinExRateGetterHour{	
	
	public ParserToCSVHourModel1(ArrayList<String> datFileNames) throws FileNotFoundException {
		super(datFileNames);
	}	

	
	/* (non-Javadoc)
	 * @see basic.BitCoinExRateGetterDay#getDollarValDay(java.lang.String, java.lang.String)
	 */
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public double getDollarValHour(String time, String value) throws IOException, JSONException{
		double val = Long.parseLong(value);
	    long timePara = 0;
	    try {
			Date result =  Util.getTime(time);
			timePara = result.getTime()/1000;	
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	    JSONObject rateJson = readJsonFromUrl("https://winkdex.com/api/v0/price?time=" + timePara);
		double penny = rateJson.getInt("price");	
		return val*BitCoinExRateGetterHour.SATTOBIT*penny/100.0;
	}
	
	protected double getDollarValDayorHour(String time, String value) throws JSONException, IOException{
		return this.getDollarValHour(time, value);
	}
}
