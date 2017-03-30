package basic1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import basic.AddressOT;
import basic.Address;
import basic.Util;

/**
 * abstract class for all parser classes
 * @author yshi
 *
 */
public abstract class ToCSVParser {
	BlockFileLoader bfl;
	HashSet<AddressOT> addrSet = new HashSet<AddressOT>();

	
	/**
	 * Constructor
	 * @param datFileNames the array of dat file name in form of .dat
	 */
	public ToCSVParser(ArrayList<String> datFileNames){
		// Arm the blockchain file loader.
		NetworkParameters np = new MainNetParams();
		List<File> blockChainFiles = new ArrayList<File>();
		for(String name : datFileNames){
			blockChainFiles.add(new File("C:\\Users\\tsutomu\\AppData\\Roaming\\Bitcoin\\blocks\\" + name));			
		}
		this.bfl = new BlockFileLoader(np, blockChainFiles);

	}
	
	/**
	 * A helping method for the method readJsonFromUrl
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	protected static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	
	/**
	 * Get JSONObject from the URL given in parameter
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	protected JSONObject readJsonFromUrl(String url) throws IOException, JSONException{
		System.setProperty("http.agent", "Chrome");
		InputStream is = null;
		try{
			is = new URL(url).openStream();			
		}catch(Exception e){
			this.end();
			System.out.println("finish exception (some transaction not fully parsed)!");
			e.printStackTrace();
			System.exit(1);	
		}
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = ToCSVParser.readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	
	/**
	 * Where the main parsing take place
	 * @throws JSONException
	 * @throws IOException
	 */
	abstract void parse() throws JSONException, IOException;
	
	/**
	 * To be overwritten in the subclasses, obtain the dollar value of 
	 * the bitcoin at time in parameter.
	 * 
	 * @param time
	 * @param value
	 * @return dollar value 
	 * @throws JSONException
	 * @throws IOException
	 */
	abstract double getDollarValDayorHour(String time, String value) throws JSONException, IOException;
	
	/**
	 * add the data recorded so far into
	 * csv files
	 * 
	 */
	abstract void end();
	
	/**
	 * If applicable, record the addr_tag_link and addr_tag
	 * of an address
	 * 
	 * @param xputs
	 * @param address
	 * @param isInput
	 * @throws JSONException
	 * @throws IOException 
	 */
	protected void getAddTagL(JSONArray xputs, String address, boolean isInput) throws JSONException, IOException{
		JSONObject item = null;
		if(isInput){
			for(int i = 0; i < xputs.length(); i ++){
				JSONObject input = xputs.getJSONObject(i);
				if(input.has("prev_out")){
					item = input.getJSONObject("prev_out");
					if(item.has("addr") && item.getString("addr").equals(address)){
						break;
					}else{
						continue;
					}
				}
			}			
		}else{
			for(int i = 0; i < xputs.length(); i ++){
				item = xputs.getJSONObject(i);
				if(item.has("addr") && item.getString("addr").equals(address)){
					break;
				}else{
					continue;
				}
			}		
		}
		
		this.addrSet.remove(new Address(address, null, null));
		JSONObject addrObj = null;
		try{
			addrObj = this.readJsonFromUrl("https://api.blocktrail.com/v1/btc/address/" + address +"?api_key= "+ Util.apiKey);
		}catch(java.net.SocketException se){
			this.end();
			System.out.println("finish exception when getting address time!");
			System.exit(1);;
		}
		String time = addrObj.getString("first_seen");
		
		// no addr entry or no correct addr entry
		if(!(item.has("addr") && item.getString("addr").equals(address))){
			this.addrSet.add(new AddressOT(address, null, null, time));
			return;
		}
		if (item.has("addr_tag_link") || item.has("addr_tag")) {
			if (item.has("addr_tag_link") && item.has("addr_tag")) {
				System.out.println(address);
				this.addrSet.remove(new AddressOT(address, item.getString("addr_tag_link"), null, time));
				this.addrSet.remove(new AddressOT(address, null, item.getString("addr_tag"), time));
				this.addrSet.add(new AddressOT(address, item.getString("addr_tag_link"), item.getString("addr_tag"), time));
			} else if (item.has("addr_tag_link")) {
				System.out.println(address);
				this.addrSet.add(new AddressOT(address, item.getString("addr_tag_link"), null, time));
			} else {
				System.out.println(address);
				this.addrSet.add(new AddressOT(address, null, item.getString("addr_tag"), time));
			}
		}else{
			this.addrSet.add(new AddressOT(address, null, null, time));						
		}
	}
	


}
