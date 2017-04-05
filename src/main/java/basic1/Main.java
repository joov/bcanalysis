package basic1;

import java.io.IOException;
import java.util.ArrayList;

import org.bitcoinj.core.Context;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;

/**
 * The class containing a main method to use the parser classes
 * 
 * @author yshi
 *
 */
public class Main {
	public static void main(String[] args) throws JSONException, IOException{
		Context.getOrCreate(MainNetParams.get());
		ArrayList<String> strList = new ArrayList<String>(); 
		strList.add("blk00514.dat");
		ParserToCSVHourModel1 p1 = new ParserToCSVHourModel1(strList);
		p1.parse();
	}
}
