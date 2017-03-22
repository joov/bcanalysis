package basic;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * abstract class for all parser classes
 * @author yshi
 *
 */
public abstract class ToCSVParser {
	BlockFileLoader bfl;
	HashSet<Address> addrSet = new HashSet<Address>();

	
	/**
	 * Constructor
	 * @param datFileNames the array of dat file name in form of .dat
	 */
	public ToCSVParser(String[] datFileNames){
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
	abstract JSONObject readJsonFromUrl(String url) throws IOException, JSONException; 

	
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
	 */
	protected void getAddTagL(JSONArray xputs, String address, boolean isInput) throws JSONException{
		JSONObject item = null;
		if(isInput){
			for(int i = 0; i < xputs.length(); i ++){
				JSONObject input = xputs.getJSONObject(i);
				if(input.has("prev_out")){
					item = input.getJSONObject("prev_out");
					if(item.has("addr") && item.get("addr").toString().equals(address)){
						break;
					}else{
						continue;
					}
				}
			}			
		}else{
			for(int i = 0; i < xputs.length(); i ++){
				item = xputs.getJSONObject(i);
				if(item.has("addr") && item.get("addr").toString().equals(address)){
					break;
				}else{
					continue;
				}
			}		
		}
		
		this.addrSet.remove(new Address(address, null, null));
		
		// no addr entry or no correct addr entry
		if(!(item.has("addr") && item.get("addr").toString().equals(address))){
			this.addrSet.add(new Address(address, null, null));
			return;
		}
		if (item.has("addr_tag_link") || item.has("addr_tag")) {
			if (item.has("addr_tag_link") && item.has("addr_tag")) {
				System.out.println(address);
				this.addrSet.remove(new Address(address, item.get("addr_tag_link").toString(), null));
				this.addrSet.remove(new Address(address, null, item.get("addr_tag").toString()));
				this.addrSet.add(new Address(address, item.get("addr_tag_link").toString(), item.get("addr_tag").toString()));
			} else if (item.has("addr_tag_link")) {
				System.out.println(address);
				this.addrSet.add(new Address(address, item.get("addr_tag_link").toString(), null));
			} else {
				System.out.println(address);
				this.addrSet.add(new Address(address, null, item.get("addr_tag").toString()));
			}
		}else{
			this.addrSet.add(new Address(address, null, null));						
		}
	}

}
