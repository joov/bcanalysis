package basic;

import java.io.IOException;
import java.util.ArrayList;

import org.bitcoinj.core.Context;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;

/**
 * The class containing a main method to use the parser classes for model 2
 * 
 * @author yshi
 *
 */
public class Main2 {
	public static void main(String[] args) throws JSONException, IOException{
		Context.getOrCreate(MainNetParams.get());
		ArrayList<String> strList = new ArrayList<String>(); 
		for(int i = 408; i < 726; i++){
			strList.add("blk00"+i+".dat");
		}
		ParserToCSVHourModel2 p1 = new ParserToCSVHourModel2(strList);
		p1.parse();
	}
}