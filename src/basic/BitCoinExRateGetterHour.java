package basic;


/**
 * Interface for all parser which use hourly exchange rate to 
 * calculate the dollar value of the bitcoin
 * 
 * @author yshi
 *
 */
public interface BitCoinExRateGetterHour {
	static final double satToBit = 0.00000001;

	// time is always in the format of "2014-03-11T08:27:57+0000"
	double getDollarValHour(String time, String value);
}
