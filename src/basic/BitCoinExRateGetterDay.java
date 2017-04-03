package basic;

import java.io.IOException;

import org.json.JSONException;


/**
 * Interface for all parser which use daily exchange rate to 
 * calculate the dollar value of the bitcoin
 * 
 * @author yshi
 *
 */
public interface BitCoinExRateGetterDay {
	static final double SATTOBIT = 0.00000001;

	// time is always in the format of "2014-03-11T08:27:57+0000"
	double getDollarValDay(String time, String value) throws IOException, JSONException;
}
