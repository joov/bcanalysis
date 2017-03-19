package basic;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.HashSet;
import java.util.List;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.net.URL;

import org.bitcoinj.core.*;
import org.bitcoinj.utils.*;
import org.bitcoinj.params.MainNetParams;
import org.json.*;

class Address{
	private String address;
	private String addrTagLink;
	private String addrTag;
	
	Address(String address, String addrTagLink, String addrTag){
		this.address = address;
		this.addrTagLink = addrTagLink;
		this.addrTag = addrTag;
	}
	
	public String getAddr(){
		return this.address;
	}
	public String getAddLink(){
		return this.addrTagLink;
	}
	public String getAddTag(){
		return this.addrTag;
	}

	
	
	public boolean equals(Object a){
//		if (!((Address) a).address.equals(this.address)){
//			return false;
//		}
//		if((!((Address) a).addrTagLink.equals(null) && !this.addrTagLink.equals(null) 
//				&& !((Address) a).addrTagLink.equals(this.addrTagLink))
//				|| (((Address) a).addrTag.equals(null) && !this.addrTag.equals(null)
//				&& !((Address) a).addrTag.equals(this.addrTag))){
//					return false;
//		}
//		if(((Address) a).addrTagLink.equals(this.addrTagLink) && ((Address) a).addrTag.equals(this.addrTag)){
//			return true;
//		}
//		// in the case when a has a null entry and b has no null entry or vice versa
//		return (((Address) a).addrTagLink.equals(null) || !this.addrTagLink.equals(null))
//								&& (((Address) a).addrTag.equals(null) || !this.addrTag.equals(null));
		
		return ((Address) a).address.equals(this.address) 
				&& ((Address) a).addrTagLink.equals(this.addrTagLink)
				&& ((Address) a).addrTag.equals(this.addrTag);
	}
	
	public String toString(){
		return this.address +  "," + this.addrTagLink + ","+ this.addrTag;
	}
}
public class ParseFromDatModel1 {	
	private static Set<Address> addrSet = new HashSet<Address>();
	private static final double satToBit = 0.00000001;
	
	// time is always in the format of "2014-03-11T08:27:57+0000"
	private static double getDollarVal(String time, String value) throws IOException, JSONException{
		double val = Long.parseLong(value);
		String target = time.split("\\+")[0];
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    df.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
	    long timePara = 0;
	    try {
			Date result =  df.parse(target);
			timePara = result.getTime()/1000;	
			System.out.println("timePara " + timePara + " target "+target);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	    JSONObject rateJson = readJsonFromUrl("https://winkdex.com/api/v0/price?time=" + timePara);
		double penny = rateJson.getInt("price");
		System.out.println("time");
		System.out.println(time);
		System.out.println(rateJson.get("timestamp"));		
		return val*satToBit*penny/100.0;
	}
	
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		System.setProperty("http.agent", "Chrome");

		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	private static void getAddTagL(JSONArray xputs, String address, boolean isInput) throws JSONException{
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
		
		ParseFromDatModel1.addrSet.remove(new Address(address, null, null));
		
		// no addr entry or no correct addr entry
		if(!(item.has("addr") && item.get("addr").toString().equals(address))){
			ParseFromDatModel1.addrSet.add(new Address(address, null, null));
			return;
		}
		if (item.has("addr_tag_link") || item.has("addr_tag")) {
			if (item.has("addr_tag_link") && item.has("addr_tag")) {
				System.out.println(address);
				ParseFromDatModel1.addrSet.remove(new Address(address, item.get("addr_tag_link").toString(), null));
				ParseFromDatModel1.addrSet.remove(new Address(address, null, item.get("addr_tag").toString()));
				ParseFromDatModel1.addrSet.add(new Address(address, item.get("addr_tag_link").toString(), item.get("addr_tag").toString()));
			} else if (item.has("addr_tag_link")) {
				System.out.println(address);
				ParseFromDatModel1.addrSet.add(new Address(address, item.get("addr_tag_link").toString(), null));
			} else {
				System.out.println(address);
				ParseFromDatModel1.addrSet.add(new Address(address, null, item.get("addr_tag").toString()));
			}
		}else{
			ParseFromDatModel1.addrSet.add(new Address(address, null, null));						
		}
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		Context.getOrCreate(MainNetParams.get());

		// Arm the blockchain file loader.
		NetworkParameters np = new MainNetParams();
		List<File> blockChainFiles = new ArrayList<File>();
		blockChainFiles.add(new File("C:\\Users\\tsutomu\\AppData\\Roaming\\Bitcoin\\blocks\\blk00514.dat"));
		BlockFileLoader bfl = new BlockFileLoader(np, blockChainFiles);

		// define files to be written into
		PrintWriter addresses = new PrintWriter(new File("./csvs/addresses.csv"));
		StringBuilder addStr = new StringBuilder();
		addStr.append("address:ID(Addr),addr_tag_link,addr_tag\n");
		PrintWriter transactions = new PrintWriter(new File("./csvs/transactions.csv"));
		StringBuilder traStr = new StringBuilder();
		traStr.append("tranHashString:ID(Trans),time\n");
		PrintWriter inputs = new PrintWriter(new File("./csvs/inputs.csv"));
		StringBuilder inputStr = new StringBuilder();
		inputStr.append("addr:ID(SendAdd),tranHashString,value_bitcoin,value_dollar,type\n");
		PrintWriter outputs = new PrintWriter(new File("./csvs/outputs.csv"));
		StringBuilder outputStr = new StringBuilder();
		outputStr.append("addr:ID(ReceAdd),tranHashString,value_bitcoin,value_dollar,type\n");

		PrintWriter inTran = new PrintWriter(new File("./csvs/intran.csv"));
		StringBuilder inStr = new StringBuilder();
		inStr.append(":START_ID(SendAdd),:END_ID(Trans)\n");
		PrintWriter outTran = new PrintWriter(new File("./csvs/outtran.csv"));
		StringBuilder outStr = new StringBuilder();
		outStr.append(":START_ID(Trans),:END_ID(ReceAdd)\n");		
		
		// Iterate over the blocks in the dataset.
		//int counter = 0;
		long time = System.currentTimeMillis();
		boolean readBl = false;
		for (Block block : bfl) {
			//comment out if not needed (i.e. when starting from the first block of a file)
//			if(block.getHashAsString().equals("0000000000000000037bf248b1aa2ec18b0e17d579e3c0bcd7d9dd2fa46fd09a")){
//				readBl = true;
//				continue;
//			}else if(!readBl){
//				continue;
//			}
			System.out.println(block.getHashAsString());
			System.out.println(System.currentTimeMillis() - time);
			time = System.currentTimeMillis();

			JSONObject blockJson = null;
			JSONObject blockAltJson = null;
			Map<Object, JSONObject> tranFromBC = new HashMap<Object, JSONObject>(); //transaction information from blockchain.info
			try{
				blockJson = readJsonFromUrl("https://api.blocktrail.com/v1/btc/block/" + block.getHashAsString() + "/transactions?api_key=b88ae2fc47fdd1b7fd132ad189734a0c783a4f5f");
				blockAltJson = readJsonFromUrl("https://blockchain.info/rawblock/" + block.getHashAsString());
				JSONArray transAlt = blockAltJson.getJSONArray("tx");
				for(int i = 0; i < transAlt.length(); i ++){
					JSONObject ta = transAlt.getJSONObject(i);
					tranFromBC.put(ta.get("hash"), ta);
				}
				System.out.println("JSON obtained!");
			}catch(java.net.SocketException se){
				transactions.write(traStr.toString());
				transactions.close();
				inputs.write(inputStr.toString());
				inputs.close();
				outputs.write(outputStr.toString());
				outputs.close();
				inTran.write(inStr.toString());
				inTran.close();
				outTran.write(outStr.toString());
				outTran.close();
				
				for(Address ad : addrSet){
					addStr.append(ad);
					addStr.append("\n");					
				}				
				addresses.write(addStr.toString());
				addresses.close();
				System.out.println("finish exception!");
				return;
			}
			//System.out.println(blockJson);
			JSONArray tas = blockJson.getJSONArray("data");
			for(int i = 0; i < tas.length(); i ++){
				JSONObject ta = tas.getJSONObject(i);
				if(!ta.get("is_coinbase").toString().equals("true")){
					String taHash = ta.get("hash").toString();
					traStr.append(taHash);
					traStr.append(",");
					traStr.append(ta.get("time"));				
					traStr.append("\n");
					//input addr:ID(SendAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray inps = ta.getJSONArray("inputs");
					for(int j = 0; j < inps.length(); j ++){
						JSONObject inp = inps.getJSONObject(j);
						if(!inp.get("type").equals("op_return")){
							if(inp.get("type").equals("multisig")){
								JSONArray multiAdd = inp.getJSONArray("multisig_addresses");
								StringBuilder multiAddList = new StringBuilder();
								for(int k = 0; k < multiAdd.length(); k++){
									multiAddList.append(multiAdd.get(k));
									multiAddList.append(';');
									addrSet.add(new Address(multiAdd.get(k).toString(), null, null));
								}
//								addrSet.add(multiAddList.toString());
								inputStr.append(multiAddList.toString());
								inputStr.append(',');
								inputStr.append(taHash);
								inputStr.append(',');
								inputStr.append(inp.get("value"));
								inputStr.append(',');
								inputStr.append(ParseFromDatModel1.getDollarVal(ta.get("time").toString(), inp.get("value").toString()));								
								inputStr.append(',');
								inputStr.append(inp.get("type"));		
								inputStr.append("\n");		
								
								//inStr :START_ID(SendAdd),:END_ID(Trans)
								inStr.append(multiAddList.toString());
								inStr.append(',');
								inStr.append(taHash);								
								inStr.append("\n");								
							}else if(inp.has("address") && inp.get("address") != null){
								inputStr.append(inp.get("address"));
								inputStr.append(',');
								inputStr.append(taHash);
								inputStr.append(',');
								inputStr.append(inp.get("value"));
								inputStr.append(',');
								inputStr.append(ParseFromDatModel1.getDollarVal(ta.get("time").toString(), inp.get("value").toString()));								
								inputStr.append(',');
								inputStr.append(inp.get("type"));
								inputStr.append("\n");
								
								// for address, addr_tag_link,addr_tag
								JSONArray inputsArr = tranFromBC.get(taHash).getJSONArray("inputs");	 
								ParseFromDatModel1.getAddTagL(inputsArr, inp.get("address").toString(), true);
								
								//inStr :START_ID(SendAdd),:END_ID(Trans)
								inStr.append(inp.get("address"));
								inStr.append(',');
								inStr.append(taHash);								
								inStr.append("\n");	
							}
						}
					}	
					
					//output addr:ID(ReceAdd),tranHashString,value,type,addr_tag_link,addr_tag
					JSONArray outps = ta.getJSONArray("outputs");
					for(int j = 0; j < outps.length(); j ++){
						JSONObject outp = outps.getJSONObject(j);
						if(!outp.get("type").equals("op_return")){
							if(outp.get("type").equals("multisig")){
								JSONArray multiAdd = outp.getJSONArray("multisig_addresses");
								StringBuilder multiAddList = new StringBuilder();
								for(int k = 0; k < multiAdd.length(); k++){
									multiAddList.append(multiAdd.get(k));
									multiAddList.append(';');
									addrSet.add(new Address(multiAdd.get(k).toString(), null, null));
								}
								outputStr.append(multiAddList.toString());
								outputStr.append(',');
								outputStr.append(taHash);
								outputStr.append(',');
								outputStr.append(outp.get("value"));
								outputStr.append(',');
								outputStr.append(ParseFromDatModel1.getDollarVal(ta.get("time").toString(), outp.get("value").toString()));								
								outputStr.append(',');
								outputStr.append(outp.get("type"));	
								outputStr.append("\n");		
								
								//outStr :START_ID(Trans),:END_ID(ReceAdd)
								outStr.append(taHash);
								outStr.append(',');
								outStr.append(multiAddList.toString());
								outStr.append("\n");
							}else if(outp.has("address") && outp.get("address") != null){
								outputStr.append(outp.get("address"));
								outputStr.append(',');
								outputStr.append(taHash);
								outputStr.append(',');
								outputStr.append(outp.get("value"));
								outputStr.append(',');
								outputStr.append(ParseFromDatModel1.getDollarVal(ta.get("time").toString(), outp.get("value").toString()));								
								outputStr.append(',');
								outputStr.append(outp.get("type"));
								outputStr.append("\n");
								
								// for addr_tag_link,addr_tag
								JSONArray outs = tranFromBC.get(taHash).getJSONArray("out");
								ParseFromDatModel1.getAddTagL(outs, outp.get("address").toString(), false);
								
								//outStr :START_ID(Trans),:END_ID(ReceAdd)
								outStr.append(taHash);
								outStr.append(',');
								outStr.append(outp.get("address"));
								outStr.append("\n");
							}
						}
					}	
				
				
				}else{
					continue;
				}
			}
			
		}
		transactions.write(traStr.toString());
		transactions.close();
		inputs.write(inputStr.toString());
		inputs.close();
		outputs.write(outputStr.toString());
		outputs.close();
		inTran.write(inStr.toString());
		inTran.close();
		outTran.write(outStr.toString());
		outTran.close();
		
		for(Address ad : addrSet){
			addStr.append(ad);
			addStr.append("\n");					
		}				
		addresses.write(addStr.toString());
		addresses.close();
		System.out.println("finish!");

	}

}
