package basic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import org.apache.commons.lang.time.DateUtils;

/**
 * A class for general functionality and constants
 * @author yshi
 *
 */
public class Util {
	
	private static final String apiKey= BCProperties.getProperty("api.key", "b88ae2fc47fdd1b7fd132ad189734a0c783a4f5f");  //to be changed if it is different
	private static final String path= BCProperties.getProperty("data.dir",System.getProperty("user.home")+"/csvs");
	
	 // time is always in the format of "2014-03-11T08:27:57+0000"
	public static Date getTime(String time) throws ParseException{
		String target = time.split("\\+")[0];
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
	    
	    return df.parse(target);
	}
	
	public static long getLongTimeClosestMinute(String time) throws ParseException{
		Date timeInDate = Util.getTime(time);
		Date nearestMinute = DateUtils.round(timeInDate, Calendar.MINUTE);
	    return nearestMinute.getTime();
	}

	
	public static final boolean equalsWithNulls(Object o1, Object o2) {
	    if (o1==o2) return true;
	    if ((o1==null)||(o2==null)) return false;
	    return o1.equals(o2);
	  }
	
	public static String getPath() {
		return path ;
	}
	public static String getApiKey() {
		return apiKey ;
	}

}
