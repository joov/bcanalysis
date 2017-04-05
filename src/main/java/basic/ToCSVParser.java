package basic;

import java.io.BufferedReader;
//import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
//import java.util.List;

import org.apache.log4j.Logger;
//import org.bitcoinj.core.NetworkParameters;
//import org.bitcoinj.params.MainNetParams;
//import org.bitcoinj.utils.BlockFileLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jline.internal.Log;

/**
 * abstract class for all parser classes
 * @author yshi
 *
 */
public abstract class ToCSVParser {
	protected int folderCounter;
	protected String lastBlockHashFromBefore;

	protected ArrayList<String> blocklists = new ArrayList<String>();
	protected AddressSet addrSet = new AddressSet();
	protected static HashSet<AddressJSON> addrJSet = new HashSet<AddressJSON>();
	protected ArrayList<Transaction> tranSet = new ArrayList<Transaction>();
	protected ArrayList<Wallet> wallSet = new ArrayList<Wallet>();

	static final Logger LOG = Logger.getLogger(ToCSVParser.class) ;
	
	/**
	 * Constructor
	 * @param datFileNames the array of dat file name in form of .dat
	 */
	public ToCSVParser(int numBlock, boolean begin, String lastHashFromBefore, int folderCounter){
		this.folderCounter = folderCounter;
		this.lastBlockHashFromBefore = lastHashFromBefore;
		int counter = 0;
		boolean readBl = begin;
		


		try {
			BufferedReader reader = new BufferedReader(new FileReader("blockhash.txt"));
			String line;
			while ((line = reader.readLine()) != null && counter < numBlock) {
				if(!line.endsWith(".dat") ){
					if (line.equals(this.lastBlockHashFromBefore)) {  
						readBl = true;
						continue;
					}
					if(readBl){
						this.blocklists.add(line);
						counter++;						
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			LOG.error("reading exception\n"+ e);
		}
	}
	
	protected void mergeWalletIfPossible(){
		for(int i = 0; i < this.wallSet.size(); i ++){
			for(int j = i+1; j < this.wallSet.size(); j ++){
				Wallet w1 = this.wallSet.get(i);
				Wallet w2 = this.wallSet.get(j);
				if(w1.equals(w2)){
					w1.merge(this, w2);
				}
			}
		}
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
			System.out.println(url);
			is = new URL(url).openStream();			
		}catch(Exception e){
			this.end();
			LOG.error("finish exception (some transaction not fully parsed)!",e);
//			System.exit(1);	
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
	 * Where the main parsing take place
	 * @throws JSONException
	 * @throws IOException
	 */
	abstract void parse() throws JSONException, IOException;
	
	
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
	protected void addAddrWithTagL(JSONArray xputs, String address, boolean isInput) throws JSONException, IOException{
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
		
		AddressJSON addrObj = this.getAddrJSON(address);
		this.addrSet.addAddrAccJSON(address, item, addrObj);
	}
	
	protected String getLastHash(){
		return this.lastBlockHashFromBefore;
	}
	
	/**
	 * If the corresponding AddressJSON is present in this.addrJSet, 
	 * this method will return the one already present, otherwise the method
	 * will call the api to generate and return the AddressJSON, and add it to this.addrJSet
	 * 
	 * @param address
	 * @return
	 */
	protected AddressJSON getAddrJSON(String address){
		for(AddressJSON aj : ToCSVParser.addrJSet){
			if(aj.getAddr().equals(address)){
				return aj;
			}
		}
		this.mergeWalletIfPossible();  
		AddressJSON aJ = null;
		try{
			JSONObject addrObj = this.readJsonFromUrl("https://api.blocktrail.com/v1/btc/address/" + address +"?api_key="+ Util.apiKey);
			aJ = new AddressJSON(addrObj);
			ToCSVParser.addrJSet.add(aJ);
		}catch(Exception se){
			this.end();
			LOG.info("finish exception when getting address time!");
//			System.exit(1);;
		}
		return aJ;
	}
	
	
	/**
	 * adjust all transaction such that they have the new wallet as the 
	 * sending/receiving address
	 * @param w
	 */
	protected void adjustTxSetAccWallet(Wallet wOld, Wallet wNew){
		for(int i = 0; i < this.tranSet.size(); i++){
			Transaction t = this.tranSet.get(i);
			if(t.getSender().getPrimAdd().equals(wOld.getPrimAdd())){
				t.setSendWallet(wNew);
				if(t.getReceiver().getPrimAdd().equals(t.getSender().getPrimAdd())){
					this.tranSet.remove(t);
				}
			}
			if(t.getReceiver().getPrimAdd().equals(wOld.getPrimAdd())){
				t.setReceiveWallet(wNew);
				if(t.getReceiver().getPrimAdd().equals(t.getSender().getPrimAdd())){
					this.tranSet.remove(t);
				}
			}
		}
	}
	
	protected void addToWallList(Wallet toAdd){
		this.wallSet.add(toAdd);
	}
	protected void removeFromWallList(Wallet toRe){
		this.wallSet.remove(toRe);
	}
}
