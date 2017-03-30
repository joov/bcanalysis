package basic;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
	
	public ParserToCSVHourModel2(ArrayList<String> datFileNames) throws FileNotFoundException {
		super(datFileNames);
	}
	
	
	protected double getDollarValDayorHour(String time, String value) throws JSONException, IOException{
		return this.getDollarValHour(time, value);
	}

	/* (non-Javadoc)
	 * @see basic.BitCoinExRateGetterDay#getDollarValDay(java.lang.String, java.lang.String)
	 */
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public double getDollarValHour(String time, String value) throws IOException, JSONException{
		double val = Long.parseLong(value);
		String target = time.split("\\+")[0];
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
	    long timePara = 0;
	    try {
			Date result =  df.parse(target);
			timePara = result.getTime()/1000;	
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	    JSONObject rateJson = readJsonFromUrl("https://winkdex.com/api/v0/price?time=" + timePara);
		double penny = rateJson.getInt("price");	
		return val*BitCoinExRateGetterHour.SATTOBIT*penny/100.0;
	}
}
