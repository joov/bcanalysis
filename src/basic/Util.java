package basic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * A class for general functionality and constants
 * @author yshi
 *
 */
public class Util {
	
	public static final String apiKey= "b88ae2fc47fdd1b7fd132ad189734a0c783a4f5f";
	
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public static Date getTime(String time) throws ParseException{
		String target = time.split("\\+")[0];
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
	    
	    return df.parse(target);
	}

}
